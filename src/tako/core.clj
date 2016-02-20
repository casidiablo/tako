(ns tako.core
  (:require [tako.github :as github]
            [tako.slack :as slack]
            [clojure.java.io :as io]
            [clojurewerkz.propertied.properties :as props]
            [clojure.tools.logging :refer [info]])
  (:gen-class
   :methods [^:static [handler [Object] String]]))

;; read credentials from config file
(def config (-> "config.properties" io/resource props/load-from props/properties->map))
(def github-token      (config "github.token"))
(def slack-webhook-url (config "slack.webhook.url"))

(defn -handler
  "This is the function called by AWS Lambda"
  [unused]
  (info (str "Called tako from lambda... "))
  (binding [tako.github/github-token github-token
            tako.slack/slack-webhook-url slack-webhook-url]
    (slack/publish-notifications (github/poll-notifications))))


(defn -main
  "This function is included for testing purposes. It is NOT used by AWS lambda.
  It is intended to be used with `lein run your-github-token slack-url`"
  [& args]
  (info "Running tako from main...")
  (binding [tako.github/github-token (first args)
            tako.slack/slack-webhook-url (second args)]
    (while true
      (slack/publish-notifications (github/poll-notifications))
      (Thread/sleep 60000))))
