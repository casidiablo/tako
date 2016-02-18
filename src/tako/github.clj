(ns tako.github
  (:require [clj-http.client :as http]
            [slingshot.slingshot :refer [try+]]
            [clojure.tools.logging :refer [info error]]))

(def notifications-url "https://api.github.com/notifications")

;; this variable is meant to be set dynimically from tako.core
(def ^:dynamic github-token)
(defn auth-header
  "Creates a header map using the Github token as authorization"
  []
  (when (nil? github-token)
    (throw (new IllegalStateException "No github token was found. Refer to documentation.")))
  {"Authorization" (str "token " github-token)})


(defn call-github
  "Calls the Github API using a GET on the provided URL. This will use
  the default header (by calling `auth-header`) if none is provided."
  ([url] (call-github url (auth-header)))
  ([url headers]
   (http/get url {:as :json :headers headers})))


(defn get-notifications
  "Gets a list of notifications from Github as a Clojure list. If there's
  nothing new returns ``:nothing-new`. If there's an error returns `:error`"
  []
  (try+
   (call-github notifications-url)
   (catch [:status 304] _
     :nothing-new)
   (catch Object e
     (error e "Failed to call github API")
     :error)))

(defn get-new-notifications
  "Calls `get-notifications` to check whether there are new notifications.
  If so, returns a map with the keys :body (a sequence of notifications)
  and :last-modified the value of the `Last-Modified` header response."
  []
  (let [notifications (get-notifications)]
    (cond

     ;; nothing new... poll after poll-interval
     (= :nothing-new notifications)
     (info "Nothing new... waiting for")

     ;; error happened
     (= :error notifications)
     (info "Some error was raised.")

     ;; something new... parse and return
     :else
     (let [{:keys [headers body]} notifications]
       (if (zero? (count body))
         (info "Nothing new, apparently")
         {:body body
          :last-modified (headers "Last-Modified")})))))

(defn mark-as-read
  "Marks the notification inbox as read."
  [last-modified]
  (http/put notifications-url
            {:body "{}"
             :content-type :json
             :headers (auth-header)
             :as :json}))

(defn extract-metadata
  "Given a github thread object builds a map with the relevant information."
  [payload type]
  (let [is-release (= type "Release")
        user-key (if is-release :author :user)]
    {:who (get-in payload [user-key :login])
     :avatar (get-in payload [user-key :avatar_url])
     :url (:html_url payload)
     :body (:body payload)}))

(defn get-metadata
  "Calls Github on the provided endpoint and tries to build an object
  with the relevant metadata of the response.

  e.g. the endpoint can be pointing to a Pull Request comment. In that case this
  will get the name of the author, its avatar, the content of the comment, etc."
  [url type]
  (-> url
      call-github
      :body
      (extract-metadata type)))

(defn process-notification
  "Given a Github notification returns a map with its relevant information"
  [{:keys [subject repository] :as notification}]
  (info (str "Processing notification " notification))
  (let [type (:type subject)
        title (:title subject)
        url (or (:latest_comment_url subject) (:url subject))
        base {:title title
              :what type
              :repo (:name repository)
              :repo_url (:html_url repository)}]
    (merge base (get-metadata url type))))

(defn poll-notifications
  "Polls unread notifications from Github, clears the inbox and returns a
  sequence of notifications. Each item in the sequence is a map with these keys:
  - :title title of the pull request or issue
  - :what notification kind
  - :repo name of the repository
  - :repo_url url to the repository
  - :who name of the user that authors the notification
  - :avatar url to the avatar of the authors
  - :url url to the pull request or issue
  - :body content of the comment or pull request"
  []
  (let [{:keys [body last-modified]} (get-new-notifications)]
    (when body
      (let [result (doall (map process-notification body))]
        ;; being here means the messages were received... they can be marked as read
        (info (str "Marking messages as read at " last-modified))
        (mark-as-read last-modified)
        result))))
