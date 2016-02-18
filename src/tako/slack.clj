(ns tako.slack
  (:require [clojure.tools.logging :refer [info]]
            [clj-http.client :as http]))

(def colors ["#16a085" "#f39c12" "#2980b9" "#34495e" "#34495e"
             "#e67e22" "#4e904f" "#0f75bc" "#9b59b6" "#95a5a6"])

(defn choose-color
  "Deterministically choose a color for the provided repo-name"
  [repo-name]
  (-> repo-name
    hash
    (mod (count colors))
    colors))

(defn build-slack-notification
  "Given a Github notification builds a Slack message"
  [notification]
  {:username (:who notification)
   :icon_url (:avatar notification)
   :rich true
   :attachments
   [{:title (:title notification)
     :title_link (:url notification)
     :author_name (:repo notification)
     :author_link (:repo_url notification)
     :text (:body notification)
     :fallback (str (:repo notification) " - " (:title notification))
     :color (choose-color (:repo notification))
     :mrkdwn_in ["text", "pretext"]}]})

(def ^:dynamic slack-webhook-url)
(defn post-to-slack
  "Posts the provided message to the bound slack-webhook-url"
  [message]
  (when-not slack-webhook-url
    (throw (new IllegalStateException "Slack Webhook URL was not configured")))
  (info "Posting message to slack:" message)
  (let [params {:form-params message :content-type :json}
        response (http/post slack-webhook-url params)]
    (info "Slack response:" response)))

(defn publish-notifications
  "For each notification in the provided sequence, builds and publish a Slack message"
  [notifications]
  (when notifications
    (info notifications)
    (info (str "Got " (count notifications) " notifications. Sending to slack..."))
    (->> notifications
      (map build-slack-notification)
      (map post-to-slack)
      doall)))
