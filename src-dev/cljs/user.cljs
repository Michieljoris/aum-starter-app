(ns cljs.user
  (:require
   [pagora.aum.om.next :as om]
   [app.frontend.core]
   [weasel.repl :as repl]
   [taoensso.timbre :as timbre]))

;; (js/console.log "hello!!!!")

;;In Emacs with Cider do space-m-", select figwheel.main and then select the dev build.

;;Or: require pagora.aum.dev.cljs-repl, in dev.core.clj for example:
;; [pagora.aum.dev.cljs-repl :as cljs-repl]
;; Then to start the cljs-repl do (cljs-repl/start-repl) in the editor's nrepl

;; (defonce connect-to-cljs-repl
;;   (when-not (repl/alive?)
;;     (timbre/info "Connecting to weasel repl")
;;     (repl/connect "ws://localhost:9001")))
