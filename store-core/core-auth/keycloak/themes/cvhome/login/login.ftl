<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "form">
        <section id="home" class="section welcome-area bg-overlay d-flex align-items-center">
            <div class="container">
                <div class="row align-items-center justify-content-center">
                    <div class="col-12 col-lg-7">
                        <div class="welcome-intro"><h1 class="text-white">Just Login!
                            </h1>
                            <p class="text-white my-4">It's just a few steps, and you will try all amazing features.</p>
                        </div>
                    </div>
                    <div class="col-12 col-md-8 col-lg-5">
                        <div id="kc-form">
                            <div id="kc-form-wrapper">
                                <div class="contact-box bg-white text-center rounded p-4 p-sm-5 mt-5 mt-lg-0 shadow-lg">
                                    <form id="kc-form-login" class="${properties.kcFormClass!}"
                                          onsubmit="login.disabled = true; return true;" action="${url.loginAction}"
                                          method="post" novalidate="novalidate">
                                        <div class="contact-top">
                                            <h3 class="contact-title">Sign In</h3>
                                        </div>
                                        <#if messagesPerField.existsError('username','password')>
                                            <div class="text-danger">
                                                ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
                                            </div>
                                        </#if>

                                        <div class="row">

                                            <div class="col-12">
                                                <div class="${properties.kcFormGroupClass!}">
                                                    <div class="${properties.kcInputGroup!}">
                                                        <div class="input-group-prepend">
                                                            <span class="input-group-text"><svg

                                                                        class="svg-inline--fa fa-envelope-open fa-w-16"
                                                                        aria-hidden="true" focusable="false"
                                                                        data-prefix="fas" data-icon="envelope-open"
                                                                        role="img" xmlns="http://www.w3.org/2000/svg"
                                                                        viewBox="0 0 512 512" data-fa-i2svg=""><path
                                                                            fill="currentColor"
                                                                            d="M512 464c0 26.51-21.49 48-48 48H48c-26.51 0-48-21.49-48-48V200.724a48 48 0 0 1 18.387-37.776c24.913-19.529 45.501-35.365 164.2-121.511C199.412 29.17 232.797-.347 256 .003c23.198-.354 56.596 29.172 73.413 41.433 118.687 86.137 139.303 101.995 164.2 121.512A48 48 0 0 1 512 200.724V464zm-65.666-196.605c-2.563-3.728-7.7-4.595-11.339-1.907-22.845 16.873-55.462 40.705-105.582 77.079-16.825 12.266-50.21 41.781-73.413 41.43-23.211.344-56.559-29.143-73.413-41.43-50.114-36.37-82.734-60.204-105.582-77.079-3.639-2.688-8.776-1.821-11.339 1.907l-9.072 13.196a7.998 7.998 0 0 0 1.839 10.967c22.887 16.899 55.454 40.69 105.303 76.868 20.274 14.781 56.524 47.813 92.264 47.573 35.724.242 71.961-32.771 92.263-47.573 49.85-36.179 82.418-59.97 105.303-76.868a7.998 7.998 0 0 0 1.839-10.967l-9.071-13.196z"></path></svg>
                                                                <!-- <i  class="fas fa-envelope-open"></i> --></span>
                                                        </div>
                                                        <input tabindex="1" id="username" name="username"
                                                               value="${(login.username!'')}" type="text" autofocus
                                                               autocomplete="off"
                                                               aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                                                               dir="ltr"/>
                                                    </div>
                                                </div>
                                                <div class="${properties.kcFormGroupClass!}">
                                                    <div class="${properties.kcInputGroup!}">
                                                        <div class="input-group-prepend">
                                                            <span class="input-group-text"><svg

                                                                        class="svg-inline--fa fa-unlock-alt fa-w-14"
                                                                        aria-hidden="true" focusable="false"
                                                                        data-prefix="fas" data-icon="unlock-alt"
                                                                        role="img"
                                                                        xmlns="http://www.w3.org/2000/svg"
                                                                        viewBox="0 0 448 512" data-fa-i2svg=""><path
                                                                            fill="currentColor"
                                                                            d="M400 256H152V152.9c0-39.6 31.7-72.5 71.3-72.9 40-.4 72.7 32.1 72.7 72v16c0 13.3 10.7 24 24 24h32c13.3 0 24-10.7 24-24v-16C376 68 307.5-.3 223.5 0 139.5.3 72 69.5 72 153.5V256H48c-26.5 0-48 21.5-48 48v160c0 26.5 21.5 48 48 48h352c26.5 0 48-21.5 48-48V304c0-26.5-21.5-48-48-48zM264 408c0 22.1-17.9 40-40 40s-40-17.9-40-40v-48c0-22.1 17.9-40 40-40s40 17.9 40 40v48z"></path></svg>
                                                                </span>
                                                        </div>
                                                        <input tabindex="2" id="password" name="password"
                                                               type="password" autocomplete="off"
                                                               aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                                                        />
                                                    </div>
                                                </div>
                                                <div class="form-check d-md-flex">
                                                    <div class="w-50 text-left">
                                                        <input tabindex="3" id="rememberMe" name="rememberMe"
                                                               type="checkbox">
                                                        <label class="form-check-label"
                                                               for="rememberMe">${msg("rememberMe")}</label>
                                                    </div>

                                                    <#--                                                    <div class="w-50 text-md-right">-->
                                                    <#--                                                        <a href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a>-->
                                                    <#--                                                    </div>-->
                                                </div>
                                            </div>
                                            <div class="col-12">
                                                <input type="hidden" id="id-hidden-input" name="credentialId"
                                                       <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                                                <input tabindex="4" class="btn btn-bordered w-100 mt-3" name="login"
                                                       id="kc-login" type="submit" value="${msg("doLogIn")}"/>
                                            </div>
                                        </div>
                                    </form>

                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <app-shape>
                <div class="shape-bottom">
                    <svg viewBox="0 0 1920 310" version="1.1"
                         xmlns="http://www.w3.org/2000/svg"
                         class="svg replaced-svg">
                        <title>Cvhome</title>
                        <desc>Created with Sketch</desc>
                        <defs></defs>
                        <g id="Cvhome-Landing-Page" stroke="none" stroke-width="1"
                           fill="none" fill-rule="evenodd">
                            <g id="Cvhome-v1.0"
                               transform="translate(0.000000, -554.000000)" fill="#FFFFFF">
                                <path d="M-3,551 C186.257589,757.321118 319.044414,856.322454 395.360475,848.004007 C509.834566,835.526337 561.525143,796.329212 637.731734,765.961549 C713.938325,735.593886 816.980646,681.910577 1035.72208,733.065469 C1254.46351,784.220361 1511.54925,678.92359 1539.40808,662.398665 C1567.2669,645.87374 1660.9143,591.478574 1773.19378,597.641868 C1848.04677,601.75073 1901.75645,588.357675 1934.32284,557.462704 L1934.32284,863.183395 L-3,863.183395"
                                      id="Cvhome-v1.0">
                                </path>
                            </g>
                        </g>
                    </svg>
                </div>
            </app-shape>
        </section>
    </#if>

</@layout.registrationLayout>
