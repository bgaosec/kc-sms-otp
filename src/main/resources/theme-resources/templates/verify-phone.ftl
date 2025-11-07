<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=false; section>
    <#if section == "title">${msg("verifyPhoneTitle")?no_esc}</#if>
    <#if section == "header">${msg("verifyPhoneHeader")?no_esc}</#if>
    <#if section == "form">
        <form id="kc-verify-phone-form" action="${url.loginAction}" method="post">
            <div class="form-group">
                <label for="phone-number">${msg("verifyPhoneLabel")}</label>
                <input id="phone-number" name="phoneNumber" class="form-control" type="tel" value="${(pendingPhone!)?html}" placeholder="${msg("verifyPhonePlaceholder")}" />
                <#if currentPhone??>
                    <small>${msg("verifyPhoneCurrent", currentPhone)}</small>
                </#if>
            </div>
            <div class="form-group">
                <button class="btn btn-secondary" type="submit" name="send" value="true" <#if resendSeconds?? && resendSeconds gt 0>disabled</#if>>${msg("verifyPhoneSend")}</button>
                <#if resendSeconds?? && resendSeconds gt 0>
                    <small>${msg("verifyPhoneResendCountdown", resendSeconds)}</small>
                </#if>
            </div>
            <div class="form-group">
                <label for="verification-code">${msg("verifyPhoneCodeLabel")}</label>
                <input id="verification-code" name="verificationCode" class="form-control" type="text" />
            </div>
            <div class="form-group">
                <button class="btn btn-primary btn-block" type="submit">${msg("doVerify")}</button>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
