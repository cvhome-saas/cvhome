<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
    <!DOCTYPE html>
    <html class="${properties.kcHtmlClass!}"<#if realm.internationalizationEnabled> lang="${locale.currentLanguageTag}" dir="${(locale.rtl)?then('rtl','ltr')}"</#if>>

    <head>
        <meta charset="utf-8">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="robots" content="noindex, nofollow">
        <style type="text/css">
            svg:not(:root).svg-inline--fa {
                overflow: visible
            }

            .svg-inline--fa {
                display: inline-block;
                font-size: inherit;
                height: 1em;
                overflow: visible;
                vertical-align: -.125em
            }

            .svg-inline--fa.fa-lg {
                vertical-align: -.225em
            }

            .svg-inline--fa.fa-w-1 {
                width: .0625em
            }

            .svg-inline--fa.fa-w-2 {
                width: .125em
            }

            .svg-inline--fa.fa-w-3 {
                width: .1875em
            }

            .svg-inline--fa.fa-w-4 {
                width: .25em
            }

            .svg-inline--fa.fa-w-5 {
                width: .3125em
            }

            .svg-inline--fa.fa-w-6 {
                width: .375em
            }

            .svg-inline--fa.fa-w-7 {
                width: .4375em
            }

            .svg-inline--fa.fa-w-8 {
                width: .5em
            }

            .svg-inline--fa.fa-w-9 {
                width: .5625em
            }

            .svg-inline--fa.fa-w-10 {
                width: .625em
            }

            .svg-inline--fa.fa-w-11 {
                width: .6875em
            }

            .svg-inline--fa.fa-w-12 {
                width: .75em
            }

            .svg-inline--fa.fa-w-13 {
                width: .8125em
            }

            .svg-inline--fa.fa-w-14 {
                width: .875em
            }

            .svg-inline--fa.fa-w-15 {
                width: .9375em
            }

            .svg-inline--fa.fa-w-16 {
                width: 1em
            }

            .svg-inline--fa.fa-w-17 {
                width: 1.0625em
            }

            .svg-inline--fa.fa-w-18 {
                width: 1.125em
            }

            .svg-inline--fa.fa-w-19 {
                width: 1.1875em
            }

            .svg-inline--fa.fa-w-20 {
                width: 1.25em
            }

            .svg-inline--fa.fa-pull-left {
                margin-right: .3em;
                width: auto
            }

            .svg-inline--fa.fa-pull-right {
                margin-left: .3em;
                width: auto
            }

            .svg-inline--fa.fa-border {
                height: 1.5em
            }

            .svg-inline--fa.fa-li {
                width: 2em
            }

            .svg-inline--fa.fa-fw {
                width: 1.25em
            }

            .fa-layers svg.svg-inline--fa {
                bottom: 0;
                left: 0;
                margin: auto;
                position: absolute;
                right: 0;
                top: 0
            }

            .fa-layers svg.svg-inline--fa {
                -webkit-transform-origin: center center;
                transform-origin: center center
            }

            .fa-ul > li {
                position: relative
            }

            @-webkit-keyframes fa-spin {
                0% {
                    -webkit-transform: rotate(0);
                    transform: rotate(0)
                }
                100% {
                    -webkit-transform: rotate(360deg);
                    transform: rotate(360deg)
                }
            }

            @keyframes fa-spin {
                0% {
                    -webkit-transform: rotate(0);
                    transform: rotate(0)
                }
                100% {
                    -webkit-transform: rotate(360deg);
                    transform: rotate(360deg)
                }
            }

            :root .fa-flip-both, :root .fa-flip-horizontal, :root .fa-flip-vertical, :root .fa-rotate-180, :root .fa-rotate-270, :root .fa-rotate-90 {
                -webkit-filter: none;
                filter: none
            }

            .svg-inline--fa.fa-stack-1x {
                height: 1em;
                width: 1.25em
            }

            .svg-inline--fa.fa-stack-2x {
                height: 2em;
                width: 2.5em
            }

            .svg-inline--fa .fa-primary {
                fill: var(--fa-primary-color, currentColor);
                opacity: 1;
                opacity: var(--fa-primary-opacity, 1)
            }

            .svg-inline--fa .fa-secondary {
                fill: var(--fa-secondary-color, currentColor);
                opacity: .4;
                opacity: var(--fa-secondary-opacity, .4)
            }

            .svg-inline--fa.fa-swap-opacity .fa-primary {
                opacity: .4;
                opacity: var(--fa-secondary-opacity, .4)
            }

            .svg-inline--fa.fa-swap-opacity .fa-secondary {
                opacity: 1;
                opacity: var(--fa-primary-opacity, 1)
            }

            .svg-inline--fa mask .fa-primary, .svg-inline--fa mask .fa-secondary {
                fill: #000
            }

            .fad.fa-inverse {
                color: #fff
            }</style>

        <#if properties.meta?has_content>
            <#list properties.meta?split(' ') as meta>
                <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
            </#list>
        </#if>
        <title>${msg("loginTitle",(realm.displayName!''))}</title>
        <link rel="icon" href="${url.resourcesPath}/img/favicon.png"/>
        <#--    <#if properties.stylesCommon?has_content>-->
        <#--        <#list properties.stylesCommon?split(' ') as style>-->
        <#--            <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />-->
        <#--        </#list>-->
        <#--    </#if>-->
        <#if properties.styles?has_content>
            <#list properties.styles?split(' ') as style>
                <link href="${url.resourcesPath}/${style}" rel="stylesheet"/>
            </#list>
        </#if>
        <script type="importmap">
            {
                "imports": {
                    "alpinejs": "${url.resourcesCommonPath}/node_modules/alpinejs/dist/module.esm.js",
                "rfc4648": "${url.resourcesCommonPath}/node_modules/rfc4648/lib/rfc4648.js"
            }
        }
        </script>
        <#if properties.scripts?has_content>
            <#list properties.scripts?split(' ') as script>
                <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
            </#list>
        </#if>
        <#if scripts??>
            <#list scripts as script>
                <script src="${script}" type="text/javascript"></script>
            </#list>
        </#if>
        <script type="module">
            import {checkCookiesAndSetTimer} from "";

            checkCookiesAndSetTimer(
                "${url.ssoLoginInOtherTabsUrl?no_esc}"
            );
        </script>
    </head>

    <body id="keycloak-bg">
    <div class="${properties.kcBodyClass!}">
        <div id="kc-header" class="${properties.kcHeaderClass!}">
            <div class="container position-relative">
                <a class="navbar-brand" href="/">
                    <img src="${url.resourcesPath}/img/logo/logo-white.png" alt="brand-logo"
                         class="navbar-brand-regular">
                    <img src="${url.resourcesPath}/img/logo/logo.png" alt="sticky brand-logo"
                         class="navbar-brand-sticky">
                </a>
                <button type="button" data-toggle="navbarToggler" aria-label="Toggle navigation"
                        class="navbar-toggler d-lg-none">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="navbar-inner">
                    <button type="button" data-toggle="navbarToggler" aria-label="Toggle navigation"
                            class="navbar-toggler d-lg-none">
                        <span class="navbar-toggler-icon"></span>
                    </button>
                </div>
            </div>
        </div>
        <footer class="${properties.kcFooterClass!}">
            <div class="footer-bottom">
                <div class="container">
                    <div class="row">
                        <div class="col-12">
                            <div
                                    class="copyright-area d-flex flex-wrap justify-content-center justify-content-sm-between text-center py-4">
                                <div class="copyright-left">
                                    ${properties.kcCopyWrite!}
                                </div>
                                <div class="copyright-right">
                                    ${properties.kcCopyWriteMadeWith!}
                                    <svg class="svg-inline--fa fa-heart fa-w-16"
                                         aria-hidden="true" focusable="false" data-prefix="fas" data-icon="heart"
                                         role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"
                                         data-fa-i2svg="">
                                        <path fill="currentColor"
                                              d="M462.3 62.6C407.5 15.9 326 24.3 275.7 76.2L256 96.5l-19.7-20.3C186.1 24.3 104.5 15.9 49.7 62.6c-62.8 53.6-66.1 149.8-9.9 207.9l193.5 199.8c12.5 12.9 32.8 12.9 45.3 0l193.5-199.8c56.3-58.1 53-154.3-9.8-207.9z"></path>
                                    </svg>
                                    By <a href="#">${properties.kcCopyWriteEmail!}</a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </footer>
    </div>
    <div class="pf-v5-c-login"
         x-data="{
            open: false,
            toggle() {
                if (this.open) {
                    return this.close()
                }

                this.$refs.button.focus()

                this.open = true
            },
            close(focusAfter) {
                if (! this.open) return

                this.open = false

                focusAfter && focusAfter.focus()
            }
        }"
         x-on:keydown.escape.prevent.stop="close($refs.button)"
         x-on:focusin.window="! $refs.panel?.contains($event.target) && close()"
         x-id="['language-select']"
    >
        <div class="pf-v5-c-login__container">
            <main class="pf-v5-c-login__main">
                <header class="pf-v5-c-login__main-header">
                    <h1 class="pf-v5-c-title pf-m-3xl" id="kc-page-title"><#nested "header"></h1>
                    <#if realm.internationalizationEnabled  && locale.supported?size gt 1>
                        <div class="pf-v5-c-login__main-header-utilities">
                            <div class="pf-v5-c-form-control">
                                <select
                                        aria-label="${msg("languages")}"
                                        id="login-select-toggle"
                                        onchange="if (this.value) window.location.href=this.value"
                                >
                                    <#list locale.supported?sort_by("label") as l>
                                        <option
                                                value="${l.url}"
                                                ${(l.languageTag == locale.currentLanguageTag)?then('selected','')}
                                        >
                                            ${l.label}
                                        </option>
                                    </#list>
                                </select>
                                <span class="pf-v5-c-form-control__utilities">
                  <span class="pf-v5-c-form-control__toggle-icon">
                    <svg
                            class="pf-v5-svg"
                            viewBox="0 0 320 512"
                            fill="currentColor"
                            aria-hidden="true"
                            role="img"
                            width="1em"
                            height="1em"
                    >
                      <path
                              d="M31.3 192h257.3c17.8 0 26.7 21.5 14.1 34.1L174.1 354.8c-7.8 7.8-20.5 7.8-28.3 0L17.2 226.1C4.6 213.5 13.5 192 31.3 192z"
                      >
                      </path>
                    </svg>
                  </span>
                </span>
                            </div>
                        </div>
                    </#if>
                </header>
                <div class="pf-v5-c-login__main-body">
                    <#if !(auth?has_content && auth.showUsername() && !auth.showResetCredentials())>
                        <#if displayRequiredFields>
                            <div class="${properties.kcContentWrapperClass!}">
                                <div class="${properties.kcLabelWrapperClass!} subtitle">
                                    <span class="pf-v5-c-helper-text__item-text"><span
                                                class="pf-v5-c-form__label-required">*</span> ${msg("requiredFields")}</span>
                                </div>
                            </div>
                        </#if>
                    <#else>
                        <#if displayRequiredFields>
                            <div class="${properties.kcContentWrapperClass!}">
                                <div class="${properties.kcLabelWrapperClass!} subtitle">
                                    <span class="subtitle"><span
                                                class="required">*</span> ${msg("requiredFields")}</span>
                                </div>
                                <div class="col-md-10">
                                    <#nested "show-username">
                                    <div id="kc-username" class="${properties.kcFormGroupClass!}">
                                        <label id="kc-attempted-username">${auth.attemptedUsername}</label>
                                        <a id="reset-login" href="${url.loginRestartFlowUrl}"
                                           aria-label="${msg('restartLoginTooltip')}">
                                            <div class="kc-login-tooltip">
                                                <i class="${properties.kcResetFlowIcon!}"></i>
                                                <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
                                            </div>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        <#else>
                            <#nested "show-username">
                            <div id="kc-username" class="${properties.kcFormGroupClass!}">
                                <label id="kc-attempted-username">${auth.attemptedUsername}</label>
                                <a id="reset-login" href="${url.loginRestartFlowUrl}"
                                   aria-label="${msg('restartLoginTooltip')}">
                                    <div class="kc-login-tooltip">
                                        <i class="${properties.kcResetFlowIcon!}"></i>
                                        <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
                                    </div>
                                </a>
                            </div>
                        </#if>
                    </#if>

                    <#-- App-initiated actions should not see warning messages about the need to complete the action -->
                    <#-- during login.                                                                               -->
                    <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                        <div class="${properties.kcAlertClass!} pf-m-${(message.type = 'error')?then('danger', message.type)}">
                            <div class="pf-v5-c-alert__icon">
                                <#if message.type = 'success'><span
                                    class="${properties.kcFeedbackSuccessIcon!}"></span></#if>
                                <#if message.type = 'warning'><span
                                    class="${properties.kcFeedbackWarningIcon!}"></span></#if>
                                <#if message.type = 'error'><span
                                    class="${properties.kcFeedbackErrorIcon!}"></span></#if>
                                <#if message.type = 'info'><span class="${properties.kcFeedbackInfoIcon!}"></span></#if>
                            </div>
                            <span class="${properties.kcAlertTitleClass!} kc-feedback-text">${kcSanitize(message.summary)?no_esc}</span>
                        </div>
                    </#if>

                    <#nested "form">

                    <#if auth?has_content && auth.showTryAnotherWayLink()>
                        <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post"
                              novalidate="novalidate">
                            <div class="${properties.kcFormGroupClass!}">
                                <input type="hidden" name="tryAnotherWay" value="on"/>
                                <a href="#" id="try-another-way"
                                   onclick="document.forms['kc-select-try-another-way-form'].submit();return false;">${msg("doTryAnotherWay")}</a>
                            </div>
                        </form>
                    </#if>

                    <#if displayInfo>
                        <div id="kc-info" class="${properties.kcSignUpClass!}">
                            <div id="kc-info-wrapper" class="${properties.kcInfoAreaWrapperClass!}">
                                <#nested "info">
                            </div>
                        </div>
                    </#if>
                </div>
                <footer class="pf-v5-c-login__main-footer">
                    <#nested "socialProviders">
                </footer>
            </main>
        </div>
    </div>
    <script type="module">
        import Alpine from "alpinejs";

        Alpine.start();
    </script>
    </body>
    </html>
</#macro>
