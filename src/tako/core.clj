(ns tako.core
  (:require [tako.github :as github]
            [tako.slack :as slack]
            [clojure.tools.logging :refer [info]])
  (:gen-class
   :methods [^:static [handler [Object] String]]))

(defn -handler [unused]
  (info (str "Called tako from lambda... " unused))
  (binding [tako.github/github-token "herewego"]
    (slack/publish-notifications (github/poll-notifications))))


(defn -main
  "This function is included for testing purposes. It is NOT used by AWS lambda.
  It is intended to be used with `lein run your-github-token`"
  [& args]
  (info "Running tako from main...")
  (binding [tako.github/github-token (first args)
            tako.slack/slack-webhook-url (second args)]
    (slack/publish-notifications (github/poll-notifications))))
