(ns try-frontend-read
  (:require [bilby.util :as bu :refer [get-selected-item make-where-clause table->table-by-id]]
            [bilby.frontend.reconciler.parser.read :as bilby]
            [bilby.frontend.reconciler.parser.read-default :as read-default]
            [transcriptor :as check]
            [clojure.test :refer [deftest is are]]
            [bilby.frontend.reconciler.parser.denormalize-hooks :refer [db->tree-hooks]]
            [bilby.reconciler.parser.util :as pu]
            [reconciler.parser.read-key.item-batch] ;;don't remove
            [reconciler.parser.read-key.selected-item]
            [bilby.reconciler.parser.key.route]
            [bilby.reconciler.parser.key.client-page-state] ;; ;;don't remove
            ;; [bilby.reconciler.parser.key.selected-item]
            ;;don't remove
            [bilby.reconciler.parser.key.route]
            [om.next :as om]
            [om.util :as om-util]
            [clojure.pprint :refer [pprint]]
            [digicheck.macros :refer [if-let* when-let*]]
            [taoensso.timbre :as timbre :refer-macros [info]])
  )

(comment
  (let [_   (bilby/derive-om-query-key! :route/users :route/*)
        default-remote :bilby
        timbre-level (:level (timbre/merge-config! nil))
        _ (timbre/merge-config! {:level :debug})
        read           (read-default/make-read-fn {:default-remote default-remote})
        parser         (om/parser {:read read ;; :mutate mutate
                                   })
        app-state      (atom
                        {;; :root    {:sub-root {:template nil ;; [[:t/by-id 1]]
                         ;;                      :group    []
                         ;;                      }}
                         ;; :t/by-id {1 {:id       1 :name "template one"
                         ;;              :category [[:c/by-id 1]]}}
                         ;; :c/by-id {1 {:id       1 :name "category one"
                         ;;              :question [[:q/by-id 1]]}}
                         ;; :q/by-id {1 {:id 1 :question "question one"}}
                         :foo {:value :for-foo :bar :bax}
                         :path4 {:subpath4 [:foo '_]}
                         :path5 {:subpath5 [:foo '_]}
                         :path6 {:subpath6 [:translation/by-id 1]}
                         :path7 {:foo7 :bar7}
                         :client/key1 :value-for-client-key1
                         :client/key2 [:foo '_]
                         :client/key3 [{:id 1} {:id 2} [:translation/by-id 1] ;; [:foo '_]
                                       ]
                         :route/companies {:list {:item-batch {:company []}}}
                         :client/key4 {:id 1 :foo :bar}
                         :client/page-state {:route/translations {:table {:translation {:selected-id 2}}}
                                             :route/companies {:table {:company {:selected-id 1}}}
                                             :route/groups {:table {:group {:selected-id nil}}}
                                             :route/users {:table {:group {:selected-id nil}
                                                                   :user {:selected-id 1}}}
                                             :route/dossier-types {:table {:group {:selected-id 10}
                                                                           :dossier-type {:selected-id 1}}}

                                             }
                         :route/translations {:selected-item {:translation [[:translation/by-id 1]]}}
                         :app/page :route/dossier-types
                         :translation/by-id {1 {:id 1 :key "some-translation-key 1"}
                                             2 {:id 2 :key "some-translation-key 2" :my-join {:id 1 :foo 2}}
                                             }
                         :some-translations {:translation [[:translation/by-id 1] [:translation/by-id 2]]}
                         :ok {:fox {:id 1}}
                         })
        env   {:state app-state}
        query
        '[{:route/dossier-types
           [{:dossier-type-list
             [({:selected-item
                [{:group
                  [:id
                   :name
                   ({:dossier-type
                     [:id
                      :name
                      :group-id
                      :client-prop/deleted
                      :inactive
                      :is-dirty?
                      {:dossier-type ...}]}
                    {:where [:parent-dossier-type-id :is :null]})]}]}
               {:table :group})]}
            ;; ({:selected-item
            ;;   [{:dossier-type
            ;;     [:id
            ;;      :name
            ;;      :name-format
            ;;      :dossier-statuses
            ;;      :checklist-statuses
            ;;      :blocks
            ;;      :inactive
            ;;      :updated-at
            ;;      :created-at
            ;;      :is-dirty?
            ;;      :client-prop/deleted
            ;;      :parent-dossier-type-id
            ;;      :group-id
            ;;      {:last-updated-user [:name]}
            ;;      ({:field
            ;;        [:id
            ;;         :label
            ;;         :order
            ;;         :type
            ;;         :inactive
            ;;         :client-prop/deleted
            ;;         :dossier-type-id
            ;;         :is-dirty?]}
            ;;       {:order-by [[:order :asc]]})]}]}
            ;;  {:table :dossier-type})
            ;; {:client/copied-dossier-type
            ;;  [:id
            ;;   :name
            ;;   :name-format
            ;;   :dossier-statuses
            ;;   :checklist-statuses
            ;;   :blocks
            ;;   :inactive
            ;;   :updated-at
            ;;   :created-at
            ;;   :is-dirty?
            ;;   :client-prop/deleted
            ;;   :parent-dossier-type-id
            ;;   :group-id
            ;;   {:last-updated-user [:name]}
            ;;   ({:field
            ;;     [:id
            ;;      :label
            ;;      :order
            ;;      :type
            ;;      :inactive
            ;;      :client-prop/deleted
            ;;      :dossier-type-id
            ;;      :is-dirty?]}
            ;;    {:order-by [[:order :asc]]})]}
            ;; {:client/route-page-state
            ;;  [:clone-mode :new-dossier-type :clone-to-group-id]}
            ;; ({:client/page-state [:invalidated-fields :validate]}
            ;;  {:table :dossier-type})
            ]}]
        ;; '[{:ok [({:fox [:id :name]} {:some :params})
        ;;               {:fox-alias [:f1 :f2]}]}]
        ]

    (defmethod bilby/read [:bilby :fox]
      [{:keys [state ast query context-data]} key params]
      (list {:fox [:modified!]} params)
      )

     (defmethod bilby/read [:bilby :fox-alias]
      [{:keys [state query ast query context-data]} key params]
       (list {:fox query } {:fox-alias :params})
      )


    ;; (let [target nil
    ;;       result (parser env query nil)]
    ;;   (timbre/info "value==============================")
    ;;   (pprint result))

    (let [target default-remote
          result (parser env query target)]
      (timbre/info "remote: ==============================")
      (pprint result))
    (timbre/merge-config! {:level timbre-level})
    ))
