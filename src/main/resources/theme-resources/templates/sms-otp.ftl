<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section == "title">${msg("smsOtpTitle")?no_esc}</#if>
    <#if section == "header">${msg("smsOtpHeader")?no_esc}</#if>
    <#if section == "form">
        <form id="kc-sms-otp-form" action="${url.loginAction}" method="post">
            <div class="form-group">
                <label for="sms-code">${msg("smsOtpPrompt", maskedPhone!"***")}</label>
                <input id="sms-code" name="smsCode" class="form-control" type="text" autofocus />
            </div>
            <#if resendSeconds?? && resendSeconds gt 0>
                <p class="instruction">${msg("smsOtpResendCountdown", resendSeconds)}</p>
            </#if>
            <div class="form-group buttons">
                <button class="btn btn-primary btn-block" type="submit">${msg("doContinue")}</button>
            </div>
            <div class="form-group buttons">
                <button class="btn btn-link" name="resend" value="true" type="submit" <#if resendSeconds?? && resendSeconds gt 0>disabled</#if>>${msg("smsOtpResend")}</button>
            </div>
        </form>
    </#if>
    <#if section == "info">
        <p>${msg("smsOtpInfo")}</p>
    </#if>
</@layout.registrationLayout>
