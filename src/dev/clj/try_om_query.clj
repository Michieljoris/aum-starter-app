(ns try-om-query
  (:require
   [digicheck.common.util :as du]
   [bilby.database.inspect :as db-inspect]
   [cuerdas.core :as str]
   [bilby.database.schema :as schema]
   [app.parser-config :refer [parser-config]]
   [taoensso.timbre :as timbre :refer [error info warn]]
   [jansi-clj.core :as jansi]
   [bilby.database.jdbc-defaults :as jdbc-defaults]
   [bilby.security :as security]
   [clojure.pprint :refer [pprint]]
   [om.util :as om-util]
   [parser.core :refer [parser]]
   [clojure.inspector :as inspect]
   ;; [inspector-jay.core :as jay]

   ;; [net.cgrand.packed-printer :refer [pprint]]

   [bilby.database.schema :refer [get-schema]]

   [mount.core :as mount]
   [dc-admin.backend.app.config :refer [config]]
   [database.config :refer [db-config]]
   [database.connection :refer [db-conn]]
   [bilby.parser :as bilby]

   [bilby.database.query :as query]
   ;;Do not remove. Loads bilby multimethods for validating sql fn and processing
   ;;params/result
   [bilby.database.process-params]
   [bilby.database.validate-sql-fun]
   [bilby.database.process-result]

   ;;Do not remove. This loads methods to the bilby.parser.mutate/mutate and
   ;;bilby-parser.read/read multimethods
   [parser.read]
   [parser.mutate]
   [database.query-hooks]
   [fipp.edn :refer (pprint) :rename {pprint fipp}]
   [clojure.set :as set]
   [bilby.database.inspect :as db-inspect])
  )
;; (jay/inspect {:a 1})
;; (ns-unmap *ns* 'parser)
;; (inspect [[1 :2] {:three 3.0 :four "5"}])


        ;; query [{:qbucket [:id :name
                          ;; {:primary-qbucket [:id :name]}
                          ;; {:replica-qbucket [:id :name]}
                           ;; {:qbucket-qbucket [:id :order
                           ;;                    {:qbucket [:id :name
                           ;;                               {:qbucket-qbucket [:id :order
                           ;;                                                  {:qbucket [:id :name]}]}]}]}
                          ;; {:qbucket-question [:id :order {:question [:id :question]}]}
                          ;; {:qbucket-user [:id {:user [:id :email]}]}
                          ;; {:qbucket [:id :name]}
          ;;]
        ;;}]
        ;; query [{:qbucket-qbucket [:id :order {:qbucket [:id :name]}]}]
        ;; query [{:qbucket [:name {:qbucket [:name]}]}]
        ;; query [{:qbucket [:name {:qbucket-qbucket [:qbucket-id :sub-qbucket-id :order]} {:qbucket '...}]}]
        ;; query [{:template [:name {:template [:id :name]}]}

;; Try out the parser as actually used for the admin:
(comment
  (let [
        template-columns (into [] (:columns database.table.template/config))
        category-columns (into [] (:columns database.table.category/config))
        question-columns (into [] (:columns database.table.question/config))

        template-columns [:id]
        category-columns [:id]
        question-columns [:id]


        query [{:route/checklist-templates [{:qbucket-entry '[({:qbucket [:id :name
                                                                          ({:qbucket [:foo]}
                                                                           {:with-meta {:type :count, :ignore-where? true},
                                                                            ;; :where [:id := 798]
                                                                            :limit {:count 3}})]}
                                                               { :where [:id := 793]
                                                                ;; :limit {:count 1}
                                                                })]}]}]
        query '[{:route/checklist-templates
                 [{:qbucket-list
                   [({:qbucket
                      [({:qbucket
                         [:qbucket-qbucket/order
                          :qbucket-qbucket/unlinked
                          :qbucket-qbucket/type
                          :name
                          :id]}
                        {:with-meta :count})]}
                     {:where [:id := 793]})]}]}]
        query [{[:qbucket/by-id 41733] [:id :name]}]

                                        ; query ['({:qbucket []} {:with-meta :count})]
        ;; query '[{:route/checklist-templates
        ;;          [{:root-qbucket-list
        ;;            [({:qbucket [:id :name :template]}
        ;;              {:where
        ;;               [:and
        ;;                [[:and [[:template := 1]]]
        ;;                 [:or [[:id :like "%QM For%"] [:name :like "%QM For%"]]]
        ;;                 [:group-id := 75]]],
        ;;               :limit {:count 3, :offset 0},
        ;;               :with-meta :count,
        ;;               :order-by [[:id :asc]]})]}]}]


        ;; query '[{:route/checklist-templates
        ;;         [{:qbucket-entry
        ;;           [({:qbucket
        ;;              [:template-id
        ;;               :created-at
        ;;               :uuid
        ;;               :entry-at
        ;;               :md5-hash
        ;;               :singleton
        ;;               :primary-id
        ;;               :category-id
        ;;               :track-primary
        ;;               :last-updated-user-id
        ;;               :template
        ;;               :sync-options
        ;;               :expired-at
        ;;               :group-id
        ;;               :repo-id
        ;;               :deleted
        ;;               :updated-at
        ;;               :description
        ;;               :synced
        ;;               :auto-sync
        ;;               ({:qbucket [:id]}
        ;;                {:with-meta {:type :count}, :limit {:count 10}})]}
        ;;             {:where [:id := 793]})]}]}]


        ;; query [{:qbucket-qbucket [:id :order {:qbucket [:id :name]}]}]
        ;; query [{:qbucket [:name {:qbucket [:name]}]}]
        ;; query [(list {:qbucket [:id :name (list {:qbucket [:name
        ;;                                                    :qbucket-qbucket/type
        ;;                                                    :qbucket-qbucket/unlinked
        ;;                                                    :qbucket-qbucket/order
        ;;                                                    ;; :qbucket-qbucket/updated-at
        ;;                                                    ;; :qbucket-qbucket/created-at
        ;;                                                    ;; :qbucket-qbucket/id
        ;;                                                    ;; {:qbucket '...}
        ;;                                                    ]}
        ;;                                         ;; {:limit {:count 2}}
        ;;                                         )]}
        ;;              {:where [:id := 41733]})]
        ;; category-query {:category (into category-columns
        ;;                                 [{:question
        ;;                                   (into [:category-question/order
        ;;                                          :category-question/category-id]
        ;;                                         question-columns)}])}

        ;; query [(list  {:template (into template-columns
        ;;                                [;; {:template (into template-columns
        ;;                                 ;;                  [category-query])}
        ;;                                 ;; {:user [:id :group-id]}
        ;;                                 category-query])} {:where [:id := 2]})]
        ;; query [(list  {:template [:id :name :entry-at :expired-at :tent-subject-id :created-at :updated-at
        ;;                           (list {:category [:id :name :order
        ;;                                             {:category-question [:order
        ;;                                                                  {:question [:id ;; :question
        ;;                                                                              ]}]}
        ;;                                             ]}
        ;;                                 {:order-by [[:order :asc] [:id :asc]]})
        ;;                           ;; {:template (into template-columns
        ;;                           ;;                  [category-query])}
        ;;                           ;; (list {:user [:id :group-id]} {:where [:group-id := 200]})
        ;;                           ;; category-query
        ;;                           ]} {:where [:id := 1606]})]
        ;; query [`(admin/unlink-records ~{:table :template
        ;;                                 :id 2
        ;;                                 :other-table :user
        ;;                                 :other-ids [3 1 4]})]
        query '[(admin/link-records {:table :user, :id 1, :other-table :template, :other-ids [3824], :_post-remote {:params {:foo :bar}}})]
        query '[{:template [:id {:user [:id :name]}]}]
        query '[({:template+user [:template.id :template.name :user.group-id]} {;; :where [:user.name := "foo"]
                                                                                :order-by [[:template.id :asc]]
                                                                                :limit {:count 20 :offset 0}})]
        query '[({:template
                  [:id
                   :entry-at
                   :tent-subject-id
                   :expired-at
                   :user.group-id
                   ;; ({:category
                   ;;   [:id
                   ;;    :name
                   ;;    :order
                   ;;    {:category-question [:order {:question [:id :question]}]}]}
                   ;;  {:order-by [[:order :asc] [:id :asc]]})
                   ;; {:user [:id :name]}
                   ;; [:id
                   ;;                 :name
                   ;;                 :email
                   ;;                 :tent-subject-id
                   ;;                 :function
                   ;;                 {:group [:id :name]}]

                   ]}
                 {;; :where [:and [[:id := 24]
                  ;;               [:user.group-id := 62]]]
                  :order-by [[:id][:user.group-id :desc]
                             ]
                  ;; :limit {:count 2}
                  :custom-read :scoped-templates})]
        ;; query '[({:template [:id :name]} {:custom-read :scoped-templates})]
        
        ;; query [(list {:user [:id :email {:template [:id :name]}]} {:where [:id := 1915]})]
        ;; query [(list {:category [:id
        ;;                          :order
        ;;                          {:question [:id
        ;;                                      :category-question/order
        ;;                                      ]}]}
        ;;              {:where [:id := 2]})
        ;;        ]
        ;; query [{:template template-columns}]
        query '[{:route/checklist-templates-users
                 [{:list
                   [{:item-batch
                     [({:template [:id :name :created-at :updated-at]}
                       {
                        :with-meta :count,
                        :where [:and [[:user.group-id := 19]]],
                        :order-by [[:name :asc]],
                        :custom-read :scoped-templates,
                        :limit {:count 25, :offset 0}})]}]}
                  ;; ({:group [:id :name]} {:where [:id := 19]})
                  ]}]
        query '[{:route/checklist-templates-users
                 [{:list
                   [{:item-batch
                     [({:template [:name :created-at :updated-at]}
                       {:with-meta :count,
                        :where [:and [[:user.group-id := 3]]],
                        :order-by [[:name :asc]],
                        :custom-read :scoped-templates,
                        :limit {:count 25,}})]}]}]}]
        query '[{:route/checklist-templates-users
                 [{:list
                   [{:item-batch
                     [({:template [:id :name :created-at :updated-at]}
                       {:with-meta :count,
                        ;; :where [:and [[:user.group-id := 11]]],
                        :order-by [[:id :asc]],
                        ;; :custom-read :scoped-templates,
                        ;; :limit {:count 25, :offset 0}
                        })]}]}]}]
        query '[({:template [({:user [:id]} {:with-meta :count})]} {:where [:id :in [1] ]})]

        query '[{:route/checklist-templates-users
                 [{:selected-item
                   [({:template
                      [:entry-at
                       :expired-at
                       :tent-subject-id
                       ({:category
                         [:id
                          :name
                          :order
                          {:category-question [:order {:question [:id :question]}]}]}
                        {:order-by [[:order :asc] [:id :asc]]})
                       {:user
                        [:id
                         :name
                         :email
                         :tent-subject-id
                         :function
                         {:group [:id :name]}]}]}
                     {;; :custom-read :scoped-templates,
                      :where [:id := 5]})]}]}]

        ;;Count by join query
        qbucket-qbucket-query {:qbucket-qbucket [:id
                                                 :order
                                                 {:qbucket [:id :name]}]}

        query '[({:qbucket  [:id :name
                             ({:qbucket-qbucket []} {:with-meta :count-by-join})
                             ({:qbucket-question []} {:with-meta :count-by-join})
                             ]
                  }
                 ;; {:qbucket-question [:id :order {:question [:id :question]}]}
                 ;; {:qbucket-user [:id {:user [:id :email]}]}


                 { ;; :where [:or [[:id := 41733] [:id := 41744]]]
                  :with-meta {:type :count
                              :ignore-where? true}
                  :where [:template := 1]
                  :limit {:count 9}
                  })]
        query `[({:qbucket ~(into [:id :name :template :last-updated-user-id
                                   {:last-updated-user [:name]}]
                                  ;; '[({:qbucket-qbucket []} {:with-meta :count-by-join})
                                  ;;   ({:qbucket-question []} {:with-meta :count-by-join})]
                                  )
                  ;; [:id :name
                  ;;  ({:qbucket-question [:type :unlinked :order :id {:qbucket [:name :id]}
                  ;;                       ]}
                  ;;   { :with-meta :count-by-join
                  ;;    ;; :where [:id := 1530]
                  ;;    ;; :limit {:count 1}
                  ;;    })
                  ;;  ]
                  }
                 {:where [:template := 1]
                  ;; [:id :in [230]]
                  }
                 )]
        query '[{[:qbucket/by-id 92] [:description
                                      :inactive
                                      :last-updated-user-id
                                      :expired-at
                                      :entry-at
                                      :group-id
                                      ({:dossier-type []}
                                       {:with-meta :count, :meta-only? true})
                                      ({:user []}
                                       {:with-meta :count, :meta-only? true})
                                      {:last-updated-user [:name]}]}]
        query [{:qbucket [:id {:dossier-type [:id]}]}]
        query [{:dossier-type [:id {:dossier-type '...}]}]

        query ['(admin/link-records
                 {:table :qbucket,
                  :id 92,
                  :other-table :user,
                  :other-ids [1150],
                  :_post-remote {:param-keys (:table :id :other-table :other-ids)}})]
        query [(list  'admin/save-records

                      ;; {:some-table {"some-id" {:updates {:some :mods}
                      ;;                          :query [:some :query]}}}
                      {:mods {:qbucket {439 {:updates {:hide-remarks true}
                                             :query [:updated-at :created-at]}}},
                       ;; :_post-remote {:param-keys [;; :mods :queries
                       ;;                             ]}
                       })]
        ;; query '[(admin/unlink-records
        ;;         {:other-table :qbucket,
        ;;          :id 92,
        ;;          :table :dossier-type,
        ;;          :other-ids [1],
        ;;          :_post-remote {:param-keys (:table :id :other-table :other-ids)}})]
        ;; query [(list 'admin/save-record
        ;;              {:table :job-offer,
        ;;               :id 3,
        ;;               :query nil,
        ;;               :mods {:published false, :user {:name "Sakina Kodad"}},
        ;;               :_post-remote {:param-keys [:id :table :query]}})]
        ;; query [{:job-offer ['*]}]
        ;; query '[{:route/users
        ;;          [{:selected-item
        ;;            [{:template-search-for-user
        ;;              [({:template [{:template [:id :name :tent-subject-id]}]}
        ;;                {:where
        ;;                 [:or
        ;;                  [[:name :like "%bodem%"]
        ;;                   [:id :like "%bodem%"]
        ;;                   [:tent-subject-id :like "%bodem%"]]],
        ;;                 :order-by [[:name :asc]],
        ;;                 :limit {:count 30}})]}]}]}]
        query '[(admin/save-records
                 {:simulate? true
                  :mods {:qbucket {"qbucket-tempid1" {:updates {:name "foo7",
                                                                :group-id 10,
                                                                :type "category"},
                                                      :query [;; :updated-at
                                                              ;; :created-at
                                                              :name
                                                              ;; {:last-updated-user [:name]}
                                                              ]},
                                   "qbucket-tempid2" {:updates {:name "subfoo7",
                                                                :group-id 10,
                                                                :type "category"},
                                                      :query [;; :updated-at
                                                              ;; :created-at
                                                              :name
                                                              ;; {:last-updated-user [:name]}
                                                              ]}
                                   },
                         :qbucket-qbucket {
                                           "qq-tempid1" {:updates {:qbucket-id 435,
                                                                   :type nil,
                                                                   :sub-qbucket-id "qbucket-tempid1",
                                                                   :sub-type "qbucket",
                                                                   :unlinked false,
                                                                   :order 0.5},
                                                         :query [;; :updated-at
                                                                 ;; :created-at
                                                                 :qbucket-id :sub-qbucket-id
                                                                 ;; {:last-updated-user [:name]}
                                                                 ]}
                                           ,
                                           "qq-tempid2" {:updates {:qbucket-id "qbucket-tempid1",
                                                                   :type nil,
                                                                   :sub-qbucket-id "qbucket-tempid2",
                                                                   :sub-type "qbucket",
                                                                   :unlinked false,
                                                                   :order 0},
                                                         :query [;; :updated-at
                                                                 ;; :created-at
                                                                 :qbucket-id :sub-qbucket-id
                                                                 {:last-updated-user [:name]}]}
                                           }}})]

        query '[(admin/save-records
                 {:simulate? true

                  :queries {;; :qbucket [ :updated-at
                            ;;            :created-at
                            ;;           {:last-updated-user [:name]}
                            ;;           :name
                            ;;           ]
                            ;; :qbucket-qbucket [:qbucket-id :sub-qbucket-id]
                            }
                  :mods {:qbucket {"40e55cf0-f5ba-4e16-afc4-9eee2b8c8528" {:updates {:name "foo7",
                                                                                     :group-id 10,
                                                                                     :type "category"},
                                                                           ;; :query [;; :updated-at
                                                                           ;;         ;; :created-at
                                                                           ;;         {:last-updated-user [:name]}
                                                                           ;;         :name
                                                                           ;;         ]
                                                                           },
                                   "94bf7f10-8e72-4e16-aa05-9434a6a0ad000-8e72-4e16-aa05-9434a6a0ad00" {:updates {:name "subfoo7",
                                                                                                                  :group-id 10,
                                                                                                                  :type "category"}
                                                                                                        ;; :query [:name
                                                                                                        ;;         ;; :updated-at
                                                                                                        ;;         ;; :created-at
                                                                                                        ;;         ;; {:last-updated-user [:name]}
                                                                                                        ;;         ]
                                                                                                        }
                                   },
                         :qbucket-qbucket {
                                           "d5d738f4-5a5e-42f7-a80d-d77599886358" {:updates {:qbucket-id 435,
                                                                                             :type nil,
                                                                                             :sub-qbucket-id "40e55cf0-f5ba-4e16-afc4-9eee2b8c8528",
                                                                                             :sub-type "qbucket",
                                                                                             :unlinked false,
                                                                                             :order 0.5},
                                                                                   ;; :query [;; :updated-at
                                                                                   ;;         ;; :created-at
                                                                                   ;;         ;; {:last-updated-user [:name]}
                                                                                   ;;         :qbucket-id :sub-qbucket-id
                                                                                   ;;         ]
                                                                                   }
                                           ,
                                           "a9bc091a-0b3a-4851-b439-3a064bdf72a9" {:updates {:qbucket-id "40e55cf0-f5ba-4e16-afc4-9eee2b8c8528",
                                                                                             :type nil,
                                                                                             :sub-qbucket-id "94bf7f10-8e72-4e16-aa05-9434a6a0ad000-8e72-4e16-aa05-9434a6a0ad00",
                                                                                             :sub-type "qbucket",
                                                                                             :unlinked false,
                                                                                             :order 0},
                                                                                   ;; :query [;; :updated-at
                                                                                   ;;         ;; :created-at

                                                                                   ;;         :qbucket-id :sub-qbucket-id
                                                                                   ;;         ;; {:last-updated-user [:name]}
                                                                                   ;;         ]
                                                                                   }
                                           }
                         }})]

        ;; query [(list {:qbucket [:id {:qbucket-qbucket [:id :qbucket-id :sub-qbucket-id {:qbucket [:id]}]}]}
        ;;              {:where [:id := 600]})]
        ;; query [(list {:qbucket-qbucket [:id :qbucket-id :sub-qbucket-id {:qbucket [:id]}]}
        ;;              {:where [:id := 103]})]
        ;; query [(list 'admin/save-records {:mods {:qbucket {;; -1 {:updates {:name "foo"}
        ;;                                                    ;;     :query [:updated-at :created-at]}
        ;;                                                    "tempid" {:updates {:name "foo"
        ;;                                                                        :type "template"
        ;;                                                                        :group-id 10
        ;;                                                                        }
        ;;                                                              :query [:updated-at :created-at]}
        ;;                                                    ;; "tempid2" {:updates {:name "foo"
        ;;                                                    ;;               ;; :type "template"
        ;;                                                    ;;               :group-id 10
        ;;                                                    ;;               }
        ;;                                                    ;;     :query [:updated-at :created-at]}
        ;;                                                    }},
        ;;                                   ;; :atomic? true
        ;;                                   :simulate? true
        ;;                                    ;; :_post-remote {:param-keys [:mods :queries]}
        ;;                                   })]
        query [(list {:qbucket [;; :id :name
                                (list {:qbucket-qbucket []}
                                      {:with-meta :count})
                                ]} {:where [:and [[:type := "template"]
                                                  [:id := 1097]]]
                                    :with-meta :count})]
        query '[(admin/save-records
                 {:mods {:qbucket-question {"68f66e11-a826-4ff5-a055-df69faac2137" {:updates {:qbucket-id 59458,
                                                                                              :type nil,
                                                                                              :order 0,
                                                                                              :sub-type "question",
                                                                                              :sub-qbucket-id 59462},
                                                                                    :query nil}}}})]
        query '[( admin/save-records
                 {:mods {:qbucket-question {112386 {:updates {:order -2}, :query nil}}}})]



        query '[(admin/save-user
                 {:table :user,
                  :id 1,
                  :query nil,
                  :mods {:name "Eddy  Steenmeijer12345678"},
                  :_post-remote {:param-keys [:id :table :query]}})]

        do-query (fn [query]
                   (println "++++++++++++++++++++++++++++++++++++++++++++++++++++++")

                   (let [
                         raw-schema (schema/get-schema db-conn)
                         schema     (schema/make-condensed-schema raw-schema)
                         ;; schema (security/secure-schema  schema db-config)
                         ;; _ (pprint (get schema "users"))
                         state      (atom {:status :ok})
                         user {:id 1990 :some-user "afoobar" :role "super-admin" :group-id 62 :subgroup-ids [154]}
                         env        {:parser-config (merge config {;; :allow-root true
                                                                   :print-exceptions true
                                                                   :normalize true})
                                     :db-conn       db-conn
                                     :db-config     db-config
                                     :schema        (security/secure-schema schema db-config)
                                     :raw-schema    raw-schema
                                     :state         state
                                     :user          user}
                         ;; query '[{:foo [({:user [:id :email]} {:where [:id := 2]})]}]
                         ;; query '[(admin/save-record
                         ;;          {:table :translation, :id "31", :query nil, :mods {:nl "Foo Prioa"}})]
                         ;; query '[{:user [:id :password-expires-at {:group [:id :name]}]}]
                         ;; query '[({:admin [:id :group-id :user-id]}
                         ;;          ;; {:where [:group-id := 10]}
                         ;;          )]
                         ;; query `[(admin/delete-dossier-type {:id 5})]

                         result    (parser env query)
                         ]
                     ;; (timbre/info (db-inspect/get-join-info env :qbucket :qbucket-qbucket))
                     ;; (timbre/info (db-inspect/get-join-info env :qbucket-qbucket :qbucket))
                     ;; (timbre/info (db-inspect/_get-join-info env :dossier-type :group))
                     ;; (info "Query:" query)
                     ;; (info "Meta data for group:" )
                     ;; (pprint (:group (:foo result)))
                     ;; (pprint (meta (:group (:foo result))))
                     
                     ;; (info "Meta data for users:" )
                     ;; (pprint (get-in @state [:table-data :group/by-id 10 :user]))
                     ;; (pprint (meta (get-in @state [:table-data :group/by-id 10 :user])))
                     
                     
                     ;; (info "Meta data for user:" )
                     ;; (pprint (:user (:foo result)))
                     ;; (pprint (meta (:user (:foo result))))
                     
                     ;; (info "Meta data for group" )
                     ;; (pprint (get-in @state [:table-data :user/by-id 1 :group]))
                     ;; (pprint (meta (get-in @state [:table-data :user/by-id 1 :group])))
                     ;; (timbre/info :#pp (:category db-config))
                     ;; (info (db-inspect/_get-join-info env :template+user :user))
                     (do
                       (info "State:")
                       (pprint @state)
                       (println "----------------------------------------")
                       (info "Result:")
                       (fipp result))
                     ;; (let [
                     ;;       categories (:category (first (:template result)))
                     ;;       ;; {ordered true unordered false} (group-by #(number? (:order %)) categories)
                     ;;       ;; ordered (sort-by :order ordered)
                     ;;       ;; unordered (sort-by :id unordered)
                     ;;       ;; categories (concat ordered unordered)
                     ;;       make-category (fn [{:keys [category-question] :as category}]
                     ;;                       (let [questions (mapv (fn [{:keys [order question]}]
                     ;;                                               (assoc question :order order))
                     ;;                                             category-question)
                     ;;                             {ordered true unordered false} (group-by #(number? (:order %)) questions)
                     ;;                             ordered (sort-by :order ordered)
                     ;;                             unordered (sort-by :id unordered)
                     ;;                             questions (concat unordered ordered)]
                     ;;                         (dissoc
                     ;;                          (assoc category :questions questions)
                     ;;                          :category-question)))
                     ;;       categories (mapv make-category categories)
                     ;;       ]
                     ;;   (fipp categories))
                     ;; (inspect/inspect-tree result)
                     ;; (println)
                     ))]
    (try
      (do-query query
       
                ;; '[{:list
                ;;    [{:item-batch
                ;;      [({:company [:id :name :created-at :updated-at]}
                ;;        {:with-meta :count,
                ;;         :order-by [[:id :asc]],
                ;;         :limit {:count 25, :offset 0}})]}]}]
       
                ;; '[({:category [:id :name ({:question [:id :question]}
                ;;                           {:with-meta {:type :count :some :param}
                ;;                            :where [:question :like "%is%"]})]}
                ;;    {:limit {:count 1}
                ;;     :with-meta :count})]
                ;; '[{:foo [({:group [:id :name ({:user []}
                ;;                               {:with-meta {:type :count :ignore-where? true}
                ;;                                :where [:name :like "%Michiel%"]})]}
                ;;           {:where [:id := 10]
                ;;            :with-meta {:type :count :ignore-where? true}})]}]
                ;; [{:qbucket-entry [(list {:qbucket [:id :name
                ;;                                    (list {:qbucket [:entry-at]}
                ;;                                          {:with-meta {:type :count :ignore-where? true
                ;;                                                       }
                ;;                                           :where [:id := 41756]})
                ;;                                    ]}
                ;;                         {:where [:id := 41744]})]}]
                ;; [{:qbuckets '[({:qbucket [:id :name
                ;;                           ({:qbucket-qbucket [{:qbucket [:id :name]}]}  {:with-meta {:type :count :ignore-where? true}
                ;;                                                                          :where [:id := -1]})
                ;;                           ;; ({:qbucket []} {:with-meta :count})
                ;;                           ]}

                ;;                {:set-group-id true
                ;;                 :where [:and [[:id := 41744]
                ;;                               [:template := 1]]]})]}]
       
                ;; '[{:foo [({:user [:id
                ;;                   :email
                ;;                   {:group [:id :name]}]}
                ;;           {:limit {:offset 0 :count 2}})]}]

       
                ;; '[{:selected/user
                ;;    [{:user [:id]}
                ;;     ]}]
       
                ;; '[({:calc/count [:count]} {:table :user
                ;;                            :where [:id :< 1000]})]
                ;; '[({:user-count [:count]} {:custom-read :count-records
                ;;                            :table :user
                ;;                            :where [:id :< 3]})]

                ;; '[({:user [:name :email :group-id {:group [:id :name :theme]}]}
                ;;    {:where [:id := :u/id]})]

                ;; `[(admin/save-record {:table :dossier-type, :id 1, :mods {:name "" :child-dossier-type-id -1}})]
       

                )
      (catch Exception e
        (throw e)
        (info (jansi/red "DEBUGGING STATEMENTS ARE STILL ENABLED IN READ.CLJ!!!!"))))))


;;Build a bilby parser-env and then a bilby parser and use that
;; (comment
;;   (require '[bilby.database.schema :as schema] '[bilby.app-config :refer [config]]
;;            '[database.config :refer [db-config]] '[database.connection :refer [db-conn]]
;;            '[bilby.database.jdbc-defaults :as jdbc-defaults] )

;;      (defn test-read
;;        "Quickly test a query (read, mutation) in the repl"
;;        [query]
;;        (println "++++++++++++++++++++++++++++++++++++++++++++++++++++++")

;;        (let [raw-schema (schema/get-schema db-conn)
;;              schema (schema/make-condensed-schema raw-schema)
;;              ;; _ (pprint (get schema "users"))
;;              state (atom {:status :ok})

;;              ;; query '[{:foo [({:user [:id :email]} {:where [:id := 2]})]}]
;;              ;; query '[(admin/save-record
;;              ;;          {:table :translation, :id "31", :query nil, :mods {:nl "Foo Prioa"}})]
;;              ;; query '[{:user [:id :password-expires-at {:group [:id :name]}]}]
;;              ;; query '[({:admin [:id :group-id :user-id]}
;;              ;;          ;; {:where [:group-id := 10]}
;;              ;;          )]
;;              ;; query `[(admin/delete-dossier-type {:id 5})]
;;              bilby-parser-env (parser-env {:parser-config config
;;                                            :db-config db-config ;;description of tables, their names, joins, crud permissions and validations
;;                                            :db-conn #?(:clj db-conn :cljs @db-conn)
;;                                            :sql {:hugsql-ns "database.queries"
;;                                                  #?@(:clj [:jdbc-result-set-read-column jdbc-defaults/result-set-read-column])
;;                                                  #?@(:clj [:jdbc-sql-value jdbc-defaults/sql-value])}})
;;              bilby-parser (parser {:parser-env bilby-parser-env})
;;              env {:parser-config (merge config {:allow-root true :print-exceptions true})
;;                   :db-conn db-conn
;;                   :db-config db-config
;;                   :schema (security/secure-schema schema db-config)
;;                   :raw-schema raw-schema
;;                   :state state
;;                   :user {:id 1 :some-user "afoobar" :role "super-admin" :group-id 12 :subgroup-ids [-1]}}
;;              result (bilby-parser env query)
;;              ]
;;          (timbre/info "Query:" query)
;;          (timbre/info "Result:")
;;          (pprint result)

;;          (timbre/info "State:")
;;          (pprint @state)
;;          (println "----------------------------------------")
;;          result
;;          ))
;;      (try
;;        ;; (test-read [{:translation [:]}])
;;        (test-read
;;         '[{:route/translations
;;            [{:list
;;              [{:item-batch
;;                [({:translation [:id :key :nl :de :en]}
;;                  {:custom-read :search-translations,
;;                   :where nil,
;;                   :order-by [[:t2.group_id :desc] [:t2.nl :asc]],
;;                   :group-id 10,
;;                   :limit {:count 10, :offset 0}})]}]}]}]



;;         )
;;        (catch Exception e
;;          (throw e)
;;          (timbre/info (jansi/red "DEBUGGING STATEMENTS ARE STILL ENABLED IN READ.CLJ!!!!")))))


;; query '[(admin/save-records
;;                  {:mods {:foo {1 {:name "foo-name-2"}},
;;                          :bar {1 {:name "bar-name-2"}},
;;                          :zed {"123" {:type :zed, :name "zed-name-2"}}},
;;                   :queries {:foo [:create-at :updated-at],
;;                             :bar nil,
;;                             :zed nil}})]



