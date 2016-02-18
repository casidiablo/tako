(ns tako.core
  (:require [tako.github :as github]
            [tako.slack :as slack]
            [clojure.tools.logging :refer [info]])
  (:gen-class
   :methods [^:static [handler [Object] String]]))

(def github-token      "PLACE YOUR GITHUB TOKEN HERE")
(def slack-webhook-url "PLACE THE SLACK WEBHOOK URL HERE")

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
    (slack/publish-notifications (github/poll-notifications))))
