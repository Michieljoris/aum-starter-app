(ns clj.stch
  (:require
   ;; [stch.sql.types :as types]
   [stch.sql.format :as sql]

   [clojure.java.jdbc :as jdbc]
   ;; [stch.sql :refer :all]

   ;; [clojure.jdbc :as jdbc]
   [stch.sql.ddl :refer :all]
   )
  )

;; (ns-unmap *ns* 'columns)
(comment
  (def sql-vec
    (create
     (-> (table :accounts4)
         (integer :id :unsigned :not-null)
         (varchar :name [50])
         ;; (set' :groups ["user" "admin"] (default "user"))
         ;; (enum :status ["active" "inactive"])
         ;; (decimal :ranking '(3 1) (default 0))
         ;; (varchar :username [50])
         ;; (chr :countryCode [2] (default "US"))
         (primary-key :id)
         (index [:id])
         ;; (unique :username)
         ;; (foreign-key :orgID '(orgs orgID) :on-delete-cascade)
         )
     (engine :InnoDB)
     (collate :utf8-general-ci)))
  (print sql-vec)
  ;; => "CREATE TABLE accounts (id INT UNSIGNED NOT NULL, name VARCHAR, PRIMARY KEY(id), INDEX(id)) ENGINE=InnoDB, COLLATE=utf8_general_ci"
  (def uri "jdbc:mysql://localhost:3306/foo?user=root&password=&zeroDateTimeBehavior=convertToNull&useSSL=false&characterEncoding=UTF-8")
  (def sql-vec (drop-table :accounts))

  (jdbc/db-do-commands uri [sql-vec]))




;; (jdbc/execute conn ["insert into foo (name) values (?);" "Foo"])
