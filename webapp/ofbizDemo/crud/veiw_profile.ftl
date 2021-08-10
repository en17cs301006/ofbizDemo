<div id="partyContent" class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">${uiLabelMap.PartyContent}</li>
        </ul>
        <br class="clear" />
    </div>
    <div class="screenlet-body">

        <hr />
        <div class="label">${uiLabelMap.PartyAttachContent}</div>
        <form id="uploadPartyContent" method="post" enctype="multipart/form-data" action="<@ofbizUrl>veiwprofile.groovy</@ofbizUrl>">
        <input type="hidden" name="dataCategoryId" value="PERSONAL"/>
        <input type="hidden" name="contentTypeId" value="DOCUMENT"/>
        <input type="hidden" name="statusId" value="CTNT_PUBLISHED"/>
        <input type="hidden" name="partyId" value="${partyId}" id="contentPartyId"/>
        <input type="file" name="uploadedFile" class="required error" size="25"/>
        <div>
            <select name="partyContentTypeId" class="required error">
                <option value="">${uiLabelMap.PartySelectPurpose}</option>
                <#list partyContentTypes as partyContentType>
                <option value="${partyContentType.partyContentTypeId}">${partyContentType.get("description", locale)?default(partyContentType.partyContentTypeId)}</option>
            </#list>
        </select>
    </div>
    <div class="label">${uiLabelMap.PartyIsPublic}</div>
    <select name="isPublic">
        <option value="N">${uiLabelMap.CommonNo}</option>
        <option value="Y">${uiLabelMap.CommonYes}</option>
    </select>
    <select name="roleTypeId">
        <option value="">${uiLabelMap.PartySelectRole}</option>
        <#list roles as role>
        <option value="${role.roleTypeId}" <#if "_NA_" == role.roleTypeId>selected="selected"</#if>>${role.get("description", locale)?default(role.roleTypeId)}</option>
        </#list>
        </select>
<input type="submit" value="${uiLabelMap.CommonUpload}" />
        </form>
<div id='progress_bar'><div></div></div>
        </div>
        </div>
<script type="application/javascript">
    jQuery("#uploadPartyContent").validate({
        submitHandler: function(form) {
            <#-- call upload scripts - functions defined in PartyProfileContent.js -->
            uploadPartyContent();
            getUploadProgressStatus();
            form.submit();
        }
    });
  </script>
<div id="partyContentList">
<#if partyContent?has_content>
<table class="basic-table" cellspacing="0">
    <#list partyContent as pContent>
    <#assign content = Content.getRelatedOne("Content", false)>
    <#assign contentType = content.getRelatedOne("ContentType", true)>
    <#assign mimeType = content.getRelatedOne("MimeType", true)!>
    <#assign status = content.getRelatedOne("StatusItem", true)!>
    <#assign pcType = Content.getRelatedOne("PartyContentType", false)>
    <tr>
        <td class="button-col"><a href="<@ofbizUrl>EditPartyContents?contentId=${pContent.contentId}&amp;partyId=${pContent.partyId}&amp;partyContentTypeId=${pContent.partyContentTypeId}&amp;fromDate=${pContent.fromDate}</@ofbizUrl>">${content.contentId}</a></td>
        <td>${(pcType.get("description", locale))!}</td>
        <td>${content.contentName!}</td>
        <td>${(contentType.get("description",locale))!}</td>
        <td>${(mimeType.description)!}</td>
        <td>${(status.get("description",locale))!}</td>
        <td>${pContent.fromDate!}</td>
        <td class="button-col">
            <#if (content.contentName?has_content)>
            <a href="<@ofbizUrl>stream?contentId=${(content.contentId)!}</@ofbizUrl>" target="_blank">${uiLabelMap.CommonView}</a>
        </#if>
        <form name="removePartyContent_${pContent_index}" method="post" action="<@ofbizUrl>removePartyContent</@ofbizUrl>">
            <input type="hidden" name="contentId" value="${pContent.contentId}" />
            <input type="hidden" name="partyId" value="${pContent.partyId}" />
            <input type="hidden" name="partyContentTypeId" value="${pContent.partyContentTypeId}" />
            <input type="hidden" name="fromDate" value="${pContent.fromDate}" />
            <a href="javascript:document.removePartyContent_${pContent_index}.submit()">${uiLabelMap.CommonRemove}</a>
        </form>
    </td>
</tr>
</#list>
        </table>
<#else>
        ${uiLabelMap.PartyNoContent}
        </#if>
        </div>
<#macro maskSensitiveNumber cardNumber>
<#assign cardNumberDisplay = "">
<#if cardNumber?has_content>
<#assign size = cardNumber?length - 4>
<#if (size > 0)>
<#list 0 .. size-1 as foo>
<#assign cardNumberDisplay = cardNumberDisplay + "*">
        </#list>
<#assign cardNumberDisplay = cardNumberDisplay + cardNumber[size .. size + 3]>
<#else>
<#-- but if the card number has less than four digits (ie, it was entered incorrectly), display it in full -->
<#assign cardNumberDisplay = cardNumber>
        </#if>
        </#if>
        ${cardNumberDisplay!}
        </#macro>

<div id="partyPaymentMethod" class="screenlet">
<div class="screenlet-title-bar">
    <ul>
        <li class="h3">${uiLabelMap.AccountingPaymentMethod}</li>
        <#if security.hasEntityPermission("PAY_INFO", "_CREATE", session) || security.hasEntityPermission("ACCOUNTING", "_CREATE", session)>
        <li><a href="<@ofbizUrl>editeftaccount?partyId=${partyId}</@ofbizUrl>">${uiLabelMap.AccountingCreateEftAccount}</a></li>
        <li><a href="<@ofbizUrl>editgiftcard?partyId=${partyId}</@ofbizUrl>">${uiLabelMap.AccountingCreateGiftCard}</a></li>
        <li><a href="<@ofbizUrl>editcreditcard?partyId=${partyId}</@ofbizUrl>">${uiLabelMap.AccountingCreateCreditCard}</a></li>
        <li><a href="<@ofbizUrl>EditBillingAccount?partyId=${partyId}</@ofbizUrl>">${uiLabelMap.AccountingCreateBillingAccount}</a></li>
        <li><a href="<@ofbizUrl>AddCheckAccount?partyId=${partyId}</@ofbizUrl>">${uiLabelMap.AccountingAddCheckAccount}</a></li>
    </#if>
</ul>
<br class="clear" />
</div>
<div class="screenlet-body">
<#if paymentMethodValueMaps?has_content || billingAccounts?has_content>
<table class="basic-table" cellspacing="0">
    <#if paymentMethodValueMaps?has_content>
    <#list paymentMethodValueMaps as paymentMethodValueMap>
    <#assign paymentMethod = paymentMethodValueMap.paymentMethod/>
    <tr>
        <#if "CREDIT_CARD" == paymentMethod.paymentMethodTypeId && paymentMethodValueMap.creditCard?has_content>
        <#assign creditCard = paymentMethodValueMap.creditCard/>
        <td class="label">
            ${uiLabelMap.AccountingCreditCard}
        </td>
        <td>
            <#if creditCard.companyNameOnCard?has_content>${creditCard.companyNameOnCard}&nbsp;</#if>
        <#if creditCard.titleOnCard?has_content>${creditCard.titleOnCard}&nbsp;</#if>
            ${creditCard.firstNameOnCard}&nbsp;
    <#if creditCard.middleNameOnCard?has_content>${creditCard.middleNameOnCard}&nbsp;</#if>
        ${creditCard.lastNameOnCard}
<#if creditCard.suffixOnCard?has_content>&nbsp;${creditCard.suffixOnCard}</#if>
        &nbsp;-&nbsp;
<#if security.hasEntityPermission("PAY_INFO", "_VIEW", session) || security.hasEntityPermission("ACCOUNTING", "_VIEW", session)>
        ${creditCard.cardType}
<@maskSensitiveNumber cardNumber=creditCard.cardNumber!/>
        ${creditCard.expireDate}
<#else>
        ${Static["org.apache.ofbiz.party.contact.ContactHelper"].formatCreditCard(creditCard)}
        </#if>
<#if paymentMethod.description?has_content>(${paymentMethod.description})</#if>
<#if paymentMethod.glAccountId?has_content>(for GL Account ${paymentMethod.glAccountId})</#if>
<#if paymentMethod.fromDate?has_content>(${uiLabelMap.CommonUpdated}:&nbsp;${paymentMethod.fromDate!})</#if>
<#if paymentMethod.thruDate?has_content><b>(${uiLabelMap.PartyContactEffectiveThru}:&nbsp;${paymentMethod.thruDate})</#if>
        </td>
<td class="button-col">
</td>
        </tr>
        </#list>
        </#if>
        </table>
<#else>
        ${uiLabelMap.PartyNoPaymentMethodInformation}
        </#if>
        </div>
        </div>
<div id="partyContactInfo" class="screenlet">
<div class="screenlet-title-bar">
    <ul>
        <li class="h3">${uiLabelMap.PartyContactInformation}</li>
        <#if security.hasEntityPermission("ofbizDemo", "_CREATE", session) || userLogin.partyId == partyId>
        <li><a href="<@ofbizUrl>editcontactmech?partyId=${partyId}</@ofbizUrl>">${uiLabelMap.CommonCreateNew}</a></li>
    </#if>
</ul>
<br class="clear" />
</div>
<div class="screenlet-body">
<#if contactMeches?has_content>
<table class="basic-table" cellspacing="0">
<tr>
    <th>${uiLabelMap.PartyContactType}</th>
    <th>${uiLabelMap.PartyContactInformation}</th>
    <th>${uiLabelMap.PartyContactSolicitingOk}</th>
    <th>&nbsp;</th>
</tr>
<#list contactMeches as contactMechMap>
<#assign contactMech = contactMechMap.contactMech>
<#assign partyContactMech = contactMechMap.partyContactMech>
<tr><td colspan="4"><hr /></td></tr>
<tr>
<td class="label align-top">${contactMechMap.contactMechType.get("description",locale)}</td>
<td>
    <#list contactMechMap.partyContactMechPurposes as partyContactMechPurpose>
    <#assign contactMechPurposeType = partyContactMechPurpose.getRelatedOne("ContactMechPurposeType", true)>
    <div>
        <#if contactMechPurposeType?has_content>
        <b>${contactMechPurposeType.get("description",locale)}</b>
        <#else>
        <b>${uiLabelMap.PartyMechPurposeTypeNotFound}: "${partyContactMechPurpose.contactMechPurposeTypeId}"</b>
    </#if>
    <#if partyContactMechPurpose.thruDate?has_content>
    (${uiLabelMap.CommonExpire}: ${partyContactMechPurpose.thruDate})
</#if>
</div>
        </#list>
<#if "POSTAL_ADDRESS" = contactMech.contactMechTypeId>
<#if contactMechMap.postalAddress?has_content>
<#assign postalAddress = contactMechMap.postalAddress>
        ${setContextField("postalAddress", postalAddress)}
<#if postalAddress.geoPointId?has_content>
<#if contactMechPurposeType?has_content>
<#assign popUptitle = contactMechPurposeType.get("description", locale) + uiLabelMap.CommonGeoLocation>
        </#if>
<a href="javascript:popUp('<@ofbizUrl>GetPartyGeoLocation?geoPointId=${postalAddress.geoPointId}&partyId=${partyId}</@ofbizUrl>', '${popUptitle!}', '450', '550')" class="buttontext">${uiLabelMap.CommonGeoLocation}</a>
        </#if>
        </#if>
<#elseif "TELECOM_NUMBER" = contactMech.contactMechTypeId>
<#if contactMechMap.telecomNumber?has_content>
<#assign telecomNumber = contactMechMap.telecomNumber>
<div>
${telecomNumber.countryCode!}
<#if telecomNumber.areaCode?has_content>${telecomNumber.areaCode?default("000")}-</#if><#if telecomNumber.contactNumber?has_content>${telecomNumber.contactNumber?default("000-0000")}</#if>
<#if partyContactMech.extension?has_content>${uiLabelMap.PartyContactExt}&nbsp;${partyContactMech.extension}</#if>
<#if !telecomNumber.countryCode?has_content || telecomNumber.countryCode = "011">
<a target="_blank" href="${uiLabelMap.CommonLookupAnywhoLink}" class="buttontext">${uiLabelMap.CommonLookupAnywho}</a>
<a target="_blank" href="${uiLabelMap.CommonLookupWhitepagesTelNumberLink}" class="buttontext">${uiLabelMap.CommonLookupWhitepages}</a>
        </#if>
        </div>
        </#if>
<#elseif "EMAIL_ADDRESS" = contactMech.contactMechTypeId>
<div>
${contactMech.infoString!}
<form method="post" action="<@ofbizUrl>NewDraftCommunicationEvent</@ofbizUrl>" onsubmit="javascript:submitFormDisableSubmits(this)" name="createEmail${contactMech.infoString?replace("&#64;","")?replace("&#x40;","")?replace(".","")}">
<#if userLogin.partyId?has_content>
<input name="partyIdFrom" value="${userLogin.partyId}" type="hidden"/>
</#if>
<input name="partyIdTo" value="${partyId}" type="hidden"/>
<input name="contactMechIdTo" value="${contactMech.contactMechId}" type="hidden"/>
<input name="my" value="My" type="hidden"/>
<input name="statusId" value="COM_PENDING" type="hidden"/>
<input name="communicationEventTypeId" value="EMAIL_COMMUNICATION" type="hidden"/>
        </form><a class="buttontext" href="javascript:document.createEmail${contactMech.infoString?replace('&#64;','')?replace('&#x40;','')?replace('.','')}.submit()">${uiLabelMap.CommonSendEmail}</a>
        </div>
<#elseif "WEB_ADDRESS" = contactMech.contactMechTypeId>
<div>
${contactMech.infoString!}
<#assign openAddress = contactMech.infoString?default("")>
<#if !openAddress?starts_with("http") && !openAddress?starts_with("HTTP")><#assign openAddress = "http://" + openAddress></#if>
<a target="_blank" href="${openAddress}" class="buttontext">${uiLabelMap.CommonOpenPageNewWindow}</a>
        </div>
<#else>
<div>${contactMech.infoString!}</div>
        </#if>
<div>(${uiLabelMap.CommonUpdated}:&nbsp;${partyContactMech.fromDate})</div>
<#if partyContactMech.thruDate?has_content><div><b>${uiLabelMap.PartyContactEffectiveThru}:&nbsp;${partyContactMech.thruDate}</b></div></#if>
<#-- create cust request -->
<#if custRequestTypes??>
<form name="createCustRequestForm" action="<@ofbizUrl>createCustRequest</@ofbizUrl>" method="post" onsubmit="javascript:submitFormDisableSubmits(this)">
<input type="hidden" name="partyId" value="${partyId}"/>
<input type="hidden" name="fromPartyId" value="${partyId}"/>
<input type="hidden" name="fulfillContactMechId" value="${contactMech.contactMechId}"/>
<select name="custRequestTypeId">
    <#list custRequestTypes as type>
    <option value="${type.custRequestTypeId}">${type.get("description", locale)}</option>
</#list>
</select>
<input type="submit" class="smallSubmit" value="${uiLabelMap.PartyCreateNewCustRequest}"/>
        </form>
        </#if>
        </td>
<td valign="top"><b>(${partyContactMech.allowSolicitation!})</b></td>
<td class="button-col">
<
</form>
        </#if>
        </td>
        </tr>
        </#list>
        </table>
<#else>
        ${uiLabelMap.PartyNoContactInformation}
        </#if>
        </div>
        </div>


<#if monthsToInclude?? && totalSubRemainingAmount?? && totalOrders??>
<div id="totalOrders" class="screenlet">
<div class="screenlet-title-bar">
    <ul>
        <li class="h3">${uiLabelMap.PartyLoyaltyPoints}</li>
    </ul>
    <br class="clear" />
</div>
<div class="screenlet-body">
    ${uiLabelMap.PartyYouHave} ${totalSubRemainingAmount} ${uiLabelMap.PartyPointsFrom} ${totalOrders} ${uiLabelMap.PartyOrderInLast} ${monthsToInclude} ${uiLabelMap.CommonMonths}.
</div>
</div>
        </#if>

<div id="partyUserLogins" class="screenlet">
<div class="screenlet-title-bar">
    <ul>
        <li class="h3">${uiLabelMap.PartyUserName}</li>
        <#if security.hasEntityPermission("OfbizDemo", "_CREATE", session)>
        <li><a href="<@ofbizUrl>ProfileCreateNewLogin?partyId=${party.partyId}&amp;CANCEL_PAGE=${DONE_PAGE!}</@ofbizUrl>">${uiLabelMap.CommonCreateNew}</a></li>
    </#if>
</ul>
<br class="clear" />
</div>
<div class="screenlet-body">
<#if userLogins?has_content>
<table class="basic-table" cellspacing="0">
    <#list userLogins as userUserLogin>
    <tr>
        <td class="label">${uiLabelMap.PartyUserLogin}</td>
        <td>${userUserLogin.userLoginId}</td>
        <td>
            <#assign enabled = uiLabelMap.PartyEnabled>
            <#if "N" == (userUserLogin.enabled)?default("Y")>
            <#if userUserLogin.disabledDateTime??>
            <#assign disabledTime = userUserLogin.disabledDateTime.toString()>
            <#else>
            <#assign disabledTime = "??">
        </#if>
        <#assign enabled = uiLabelMap.PartyDisabled + " - " + disabledTime>
    </#if>
            ${enabled}
</td>
<td class="button-col">
    <#if security.hasEntityPermission("ofbizDemo", "_CREATE", session)>
    <a href="<@ofbizUrl>ProfileEditUserLogin?partyId=${party.partyId}&amp;userLoginId=${userUserLogin.userLoginId}</@ofbizUrl>">${uiLabelMap.CommonEdit}</a>
</#if>
<#if security.hasEntityPermission("SECURITY", "_VIEW", session)>
<a href="<@ofbizUrl>ProfileEditUserLoginSecurityGroups?partyId=${party.partyId}&amp;userLoginId=${userUserLogin.userLoginId}</@ofbizUrl>">${uiLabelMap.SecurityGroups}</a>
</#if>
        </td>
        </tr>
        </#list>
        </table>
<#else>
        ${uiLabelMap.PartyNoUserLogin}
        </#if>
        </div>
        </div>
