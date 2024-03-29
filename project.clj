(defproject bilgge "0.1.4-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs "2.11.7"]
                 [reagent "0.10.0"]
                 [re-frame "1.1.2"]
                 [day8.re-frame/tracing "0.6.0"]
                 [day8.re-frame/http-fx "0.2.2"]
                 [cljs-ajax "0.8.0"]
                 [metosin/reitit-core "0.5.11"]
                 [metosin/reitit-frontend "0.5.11"]]

  :plugins [[lein-shadow "0.3.1"]
            [lein-shell "0.5.0"]
            [lein-cljfmt "0.7.0"]
            [lein-kibit "0.1.8"]]

  :min-lein-version "2.9.0"

  :source-paths ["src/clj" "src/cljs"]

  :test-paths   ["test/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]


  :shadow-cljs {:nrepl {:port 8777}
                
                :builds {:app
                         {:target :browser
                          :output-dir "resources/public/js/compiled"
                          :asset-path "/js/compiled"
                          :modules {:app {:init-fn bilgge.core/init
                                          :preloads [devtools.preload
                                                     day8.re-frame-10x.preload]}}
                          :dev {:compiler-options {:closure-defines {re-frame.trace.trace-enabled? true
                                                                     day8.re-frame.tracing.trace-enabled? true
                                                                     bilgge.api/API-BASE-URL ~(System/getenv "API_BASE_URL")}}}
                          :release {:build-options {:ns-aliases {day8.re-frame.tracing day8.re-frame.tracing-stubs}}
                                    :compiler-options {:closure-defines {bilgge.api/API-BASE-URL ~(System/getenv "API_BASE_URL")}}}

                          :devtools {:http-root "resources/public"
                                     :http-port 8280}}

                         :browser-test
                         {:target :browser-test
                          :ns-regexp "-test$"
                          :runner-ns shadow.test.browser
                          :test-dir "target/browser-test"
                          :devtools {:http-root "target/browser-test"
                                     :http-port 8290}}

                         :karma-test
                         {:target :karma
                          :ns-regexp "-test$"
                          :output-to "target/karma-test.js"
                          :closure-defines {bilgge.api/API-BASE-URL ~(System/getenv "API_BASE_URL")}}

                         :pact
                         {:target :node-script
                          :main cljs.script/main
                          :output-to "target/pack.js"}}}
  
  :shell {:commands {"karma" {:windows         ["cmd" "/c" "karma"]
                              :default-command "karma"}
                     "open"  {:windows         ["cmd" "/c" "start"]
                              :macosx          "open"
                              :linux           "xdg-open"}}}

  :aliases {"dev"          ["do" 
                            ["shell" "echo" "\"DEPRECATED: Please use lein watch instead.\""]
                            ["watch"]]
            "watch"        ["with-profile" "dev" "do"
                            ["shadow" "watch" "app" "browser-test" "karma-test" "pact"]]

            "prod"         ["do"
                            ["shell" "echo" "\"DEPRECATED: Please use lein release instead.\""]
                            ["release"]]

            "release"      ["with-profile" "prod" "do"
                            ["shadow" "release" "app"]]

            "build-report" ["with-profile" "prod" "do"
                            ["shadow" "run" "shadow.cljs.build-report" "app" "target/build-report.html"]
                            ["shell" "open" "target/build-report.html"]]

            "karma"        ["do"
                            ["shell" "echo" "\"DEPRECATED: Please use lein ci instead.\""]
                            ["ci"]]
            "ci"           ["with-profile" "dev" "do"
                            ["shadow" "compile" "karma-test"]
                            ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]
            "pact-mock"    ["with-profile" "script" "do"
                            ["shadow" "compile" "pact"]
                            ["shell" "node" "target/pack.js" "server"]]
            "pact-publish" ["with-profile" "script" "do"
                            ["shadow" "compile" "pact"]
                            ["shell" "node" "target/pack.js" "publish"]]
            "ci-compile"   ["with-profile" "dev" "do"
                            ["shadow" "compile" "app" "karma-test" "pact"]]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "1.0.2"]
                   [day8.re-frame/re-frame-10x "0.7.0"]
                   [day8.re-frame/test "0.1.5"]]
    :source-paths ["dev" "script"]}

   :script
   {:source-paths ["script"]}

   :prod {}
}

  :prep-tasks [])
