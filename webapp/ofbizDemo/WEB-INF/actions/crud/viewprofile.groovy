/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


/**
 * This script finds and places in the context of the form current (un-expired) contact mechs for the logged in user and the
 * party for whom the communication event is intended.  It currently just does searches for email but should be
 * expanded to work off other communication event types.
 */

import org.apache.ofbiz.base.util.UtilDateTime

partyIdFrom = context.partyIdFrom
partyIdTo = context.partyIdTo

if (parameters.communicationEventTypeId) {
    if ("EMAIL_COMMUNICATION".equals(parameters.communicationEventTypeId)) {
        userEmailAddresses = from("PartyContactWithPurpose").where("contactMechTypeId", "EMAIL_ADDRESS" , "partyId", partyIdFrom).filterByDate(UtilDateTime.nowTimestamp(), "contactFromDate", "contactThruDate").queryList()
        context.userEmailAddresses = userEmailAddresses

        targetEmailAddresses = from("PartyContactWithPurpose").where("contactMechTypeId", "EMAIL_ADDRESS", "partyId", partyIdTo).filterByDate(UtilDateTime.nowTimestamp(), "contactFromDate", "contactThruDate").queryList()
        context.targetEmailAddresses = targetEmailAddresses
    }
}import org.apache.ofbiz.base.component.ComponentConfig

if (parameters.communicationEventId) {
    context.communicationEventRole = from("CommunicationEventRole")
            .where("communicationEventId", parameters.communicationEventId, "partyId", parameters.partyId, "roleTypeId", parameters.roleTypeId)
            .queryOne()

    context.projectMgrExists = ComponentConfig.componentExists("projectmgr")
}
communicationEvent = from("CommunicationEvent").where("communicationEventId", parameters.communicationEventId).cache(true).queryOne()

if (!communicationEvent.note) return
nameString = "Sent from: "
nameStringIndexValue = communicationEvent.note.indexOf(nameString)
if (nameStringIndexValue == -1) return
int startEmail = nameStringIndexValue + nameString.length()
int endEmail = communicationEvent.note.indexOf(";", startEmail)
context.emailAddress = communicationEvent.note.substring(startEmail, endEmail)

nameString = "Sent Name from: "
nameStringIndexValue = communicationEvent.note.indexOf(nameString)
if (nameStringIndexValue == -1) return
int startName = nameStringIndexValue + nameString.length()
int endName = communicationEvent.note.indexOf(";", startName)
name = communicationEvent.note.substring(startName, endName)
if (name) {
    counter = 0
    lastBlank = 0
    List names = []
    while ((nextBlank = name.indexOf(" ", lastBlank)) != -1) {
        names.add(name.substring(lastBlank, nextBlank))
        lastBlank = nextBlank + 1
    }
    if (lastBlank > 0) {
        names.add(name.substring(lastBlank))
    }
    if (names && names.size() > 0) { //lastname
        context.lastName = names[names.size()-1]
        if (names.size() > 1) { // firstname
            context.firstName = names[0]
        }
        if (names.size() > 2) { // middle name(s)
            context.middleName = ""
            for (counter = 1; counter < names.size()-1; counter++) {
                context.middleName = context.middleName.concat(names[counter]).concat(" ")
            }
        }
    }  else {
        context.lastName = name
    }
}
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.condition.EntityCondition

partyId = parameters.partyId
context.partyId = partyId

party = from("Party").where("partyId", partyId).queryOne()
context.party = party

// get the sort field
sortField = parameters.sort ?: "entryDate"
context.previousSort = sortField

// previous sort field
previousSort = parameters.previousSort
if (previousSort?.equals(sortField)) {
    sortField = "-" + sortField
}

List eventExprs = []
expr = EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, partyId)
eventExprs.add(expr)
expr = EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, partyId)
eventExprs.add(expr)
ecl = EntityCondition.makeCondition(eventExprs, EntityOperator.OR)
events = from("CommunicationEvent").where(ecl).orderBy(sortField).queryList()

context.eventList = events
context.eventListSize = events.size()
context.highIndex = events.size()
context.viewSize = events.size()
context.lowIndex = 1
context.viewIndex = 1

import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.entity.util.EntityUtil

lastDate = UtilDateTime.addDaysToTimestamp(UtilDateTime.nowTimestamp(), -21) // should be there the last 3 weeks.
visits = select('partyId')
        .from('Visit')
        .where(EntityCondition.makeCondition("fromDate", EntityOperator.GREATER_THAN, lastDate))
        .distinct()
        .queryList()
partyIds = EntityUtil.getFieldListFromEntityList(visits, 'partyId', false)
context.recentParties = select("partyId", "firstName", "middleName", "lastName", "groupName")
        .from("PartyNameView")
        .where(EntityCondition.makeCondition('partyId', EntityOperator.IN, partyIds))
        .distinct()
        .queryList()
import org.apache.ofbiz.party.contact.ContactMechWorker

partyId = parameters.partyId
context.partyId = partyId

Map mechMap = new HashMap()
ContactMechWorker.getContactMechAndRelated(request, partyId, mechMap)
context.mechMap = mechMap

context.contactMechId = mechMap.contactMechId
context.preContactMechTypeId = parameters.preContactMechTypeId
context.paymentMethodId = parameters.paymentMethodId

cmNewPurposeTypeId = parameters.contactMechPurposeTypeId
if (cmNewPurposeTypeId) {
    contactMechPurposeType = from("ContactMechPurposeType").where("contactMechPurposeTypeId", cmNewPurposeTypeId).queryOne()
    if (contactMechPurposeType) {
        context.contactMechPurposeType = contactMechPurposeType
    } else {
        cmNewPurposeTypeId = null
    }
    context.cmNewPurposeTypeId = cmNewPurposeTypeId
}
context.donePage = parameters.DONE_PAGE ?:"viewprofile?party_id=" + partyId + "&partyId=" + partyId


import org.apache.ofbiz.accounting.payment.PaymentWorker
import org.apache.ofbiz.party.contact.ContactMechWorker

partyId = parameters.partyId ?: parameters.party_id
context.partyId = partyId

// payment info
paymentResults = PaymentWorker.getPaymentMethodAndRelated(request, partyId)
//returns the following: "paymentMethod", "creditCard", "giftCard", "eftAccount", "paymentMethodId", "curContactMechId", "donePage", "tryEntity"
context.putAll(paymentResults)

curPostalAddressResults = ContactMechWorker.getCurrentPostalAddress(request, partyId, paymentResults.curContactMechId)
//returns the following: "curPartyContactMech", "curContactMech", "curPostalAddress", "curPartyContactMechPurposes"
context.putAll(curPostalAddressResults)

context.postalAddressInfos = ContactMechWorker.getPartyPostalAddresses(request, partyId, paymentResults.curContactMechId)

//prepare "Data" maps for filling form input boxes
tryEntity = paymentResults.tryEntity

creditCardData = paymentResults.creditCard
if (!tryEntity) creditCardData = parameters
context.creditCardData = creditCardData ?:[:]

giftCardData = paymentResults.giftCard
if (!tryEntity) giftCardData = parameters
context.giftCardData = giftCardData ?: [:]

eftAccountData = paymentResults.eftAccount
if (!tryEntity) eftAccountData = parameters
context.eftAccountData = eftAccountData ?: [:]

checkAccountData = paymentResults.checkAccount
if (!tryEntity) checkAccountData = parameters
context.checkAccountData = checkAccountData ?: [:]

context.donePage = parameters.DONE_PAGE ?:"viewprofile"

paymentMethodData = paymentResults.paymentMethod
if (!tryEntity.booleanValue()) paymentMethodData = parameters
if (!paymentMethodData) paymentMethodData = new HashMap()
if (paymentMethodData) context.paymentMethodData = paymentMethodData


import org.apache.ofbiz.entity.util.EntityUtil
import org.apache.ofbiz.base.util.UtilHttp
import org.apache.ofbiz.product.catalog.CatalogWorker
import org.apache.ofbiz.webapp.website.WebSiteWorker
import org.apache.ofbiz.entity.util.EntityUtilProperties


prodCatalogId = CatalogWorker.getCurrentCatalogId(request)
webSiteId = WebSiteWorker.getWebSiteId(request)

currencyUomId = parameters.currencyUomId ?: UtilHttp.getCurrencyUom(request)
context.currencyUomId = currencyUomId

partyId = parameters.partyId ?:request.getAttribute("partyId")

party = from("Party").where("partyId", partyId).queryOne()
context.party = party
if (party) {
    context.lookupPerson = party.getRelatedOne("Person", false)
    context.lookupGroup = party.getRelatedOne("PartyGroup", false)
}
import org.apache.ofbiz.base.util.Debug
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityUtilProperties

if (context.noConditionFind == null) {
    context.noConditionFind = parameters.noConditionFind
}
if (context.noConditionFind == null) {
    context.noConditionFind = EntityUtilProperties.getPropertyValue("widget", "widget.defaultNoConditionFind", delegator)
}
if (context.filterByDate == null) {
    context.filterByDate = parameters.filterByDate
}
prepareResult = runService('prepareFind', [entityName : context.entityName,
                                           orderBy : context.orderBy,
                                           inputFields : parameters,
                                           filterByDate : context.filterByDate,
                                           filterByDateValue : context.filterByDateValue,
                                           userLogin : context.userLogin])

exprList = [EntityCondition.makeCondition("statusId", null),
            EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PARTY_DISABLED")]
statusPartyDisable = EntityCondition.makeCondition(exprList, EntityOperator.OR)
entityConditionList = null
if (prepareResult.entityConditionList != null) {
    ConditionList = [prepareResult.entityConditionList, statusPartyDisable]
    entityConditionList = EntityCondition.makeCondition(ConditionList)
} else if (context.noConditionFind == "Y") {
    entityConditionList = statusPartyDisable
}

executeResult = runService('executeFind', [entityName : context.entityName,
                                           orderByList : prepareResult.orderByList,
                                           entityConditionList : entityConditionList,
                                           noConditionFind : context.noConditionFind])
if (executeResult.listIt == null) {
    Debug.logWarning("No list found for query string + [" + prepareResult.queryString + "]", "FindLookUp.groovy")
}
context.listIt = executeResult.listIt
context.queryString = prepareResult.queryString
context.queryStringMap = prepareResult.queryStringMap

import org.apache.ofbiz.party.party.PartyWorker

match = parameters.match
if (match) {
    context.match = match

    lastName = parameters.lastName ?: null
    firstName = parameters.firstName ?: null
    address1 = parameters.address1 ?: null
    address2 = parameters.address2 ?: null
    city = parameters.city ?: null
    state = parameters.stateProvinceGeoId ?: null
    if ("ANY".equals(state)) state = null
    postalCode = parameters.postalCode ?: null

    if (state) {
        context.currentStateGeo = from("Geo").where("geoId", state).queryOne()
    }

    if (!firstName || !lastName || !address1 || !city || !postalCode) {
        request.setAttribute("_ERROR_MESSAGE_", "Required fields not set!")
        return
    }

    context.matches = PartyWorker.findMatchingPersonPostalAddresses(delegator, address1, address2, city,
            state, postalCode, null, null, firstName, null, lastName)

    context.addressString = PartyWorker.makeMatchingString(delegator, address1)
    context.lastName = lastName
    context.firstName = firstName
}
if (!context.extInfo || context.extInfo == 'N') {
    if (parameters.partyIdentificationTypeId || parameters.idValue) context.extInfo='I'
    else if (parameters.address1
            || parameters.address2
            || parameters.city
            || parameters.postalCode
            || parameters.stateProvinceGeoId) context.extInfo='P'
    else if (parameters.countryCode
            || parameters.areaCode
            || parameters.contactNumber) context.extInfo='T'
    else if (parameters.infoString) context.extInfo='O'
    if (!context.extInfo) context.extInfo == 'N'
}
if (!context.extInfo || context.extInfo == 'N') {
    if (parameters.partyIdentificationTypeId || parameters.idValue) context.extInfo='I'
    else if (parameters.address1
            || parameters.address2
            || parameters.city
            || parameters.postalCode
            || parameters.stateProvinceGeoId) context.extInfo='P'
    else if (parameters.countryCode
            || parameters.areaCode
            || parameters.contactNumber) context.extInfo='T'
    else if (parameters.infoString) context.extInfo='O'
    if (!context.extInfo) context.extInfo == 'N'
}

import org.apache.ofbiz.party.contact.ContactMechWorker

partyId = partyId ?: parameters.partyId
showOld = "true".equals(parameters.SHOW_OLD)
context.contactMeches = ContactMechWorker.getPartyContactMechValueMaps(delegator, partyId, showOld)

partyId = partyId ?: parameters.partyId

savedCart = from("ShoppingList").where("partyId", partyId, "shoppingListTypeId", "SLT_SPEC_PURP" , "listName", "auto-save").queryFirst()

if (savedCart) {
    context.savedCartListId = savedCart.shoppingListId
    context.savedCartItems = savedCart.getRelated("ShoppingListItem", null, null, false)
}
import org.apache.ofbiz.common.geo.GeoWorker

if (partyId) {
    context.partyId = partyId
    latestGeoPoint = GeoWorker.findLatestGeoPoint(delegator, "PartyAndGeoPoint", "partyId", partyId, null, null)
    if (latestGeoPoint) {
        context.geoPointId = latestGeoPoint.geoPointId
        context.latitude = latestGeoPoint.latitude
        context.longitude = latestGeoPoint.longitude
    } else {
        context.latitude = 0
        context.longitude = 0
    }
}

partyId = parameters.partyId ? parameters.partyId : userLogin.partyId

if (partyId) {
    // get the system user
    system = from("UserLogin").where("userLoginId", "system").queryOne()

    monthsToInclude = 12

    Map result = runService('getOrderedSummaryInformation', ["partyId": partyId, "roleTypeId": "PLACING_CUSTOMER", "orderTypeId": "SALES_ORDER",
                                                             "statusId": "ORDER_COMPLETED", "monthsToInclude": monthsToInclude, "userLogin": system])

    context.monthsToInclude = monthsToInclude
    context.totalSubRemainingAmount = result.totalSubRemainingAmount
    context.totalOrders = result.totalOrders
}
if (userLogin) {
    companies = from("PartyRelationship").where(partyIdTo: userLogin.partyId, roleTypeIdTo: "CONTACT", roleTypeIdFrom: "ACCOUNT").queryList()
    if (companies) {
        company = companies[0]
        context.myCompanyId = company.partyIdFrom
    } else {
        context.myCompanyId = userLogin.partyId
    }
}

import org.apache.ofbiz.accounting.payment.PaymentWorker
import org.apache.ofbiz.accounting.payment.BillingAccountWorker

partyId = parameters.partyId ?: userLogin.partyId
showOld = "true".equals(parameters.SHOW_OLD)

currencyUomId = null
billingAccounts = []
if (partyId) {
    billingAccountAndRoles = from("BillingAccountAndRole").where("partyId", partyId).queryList()
    if (billingAccountAndRoles) currencyUomId = billingAccountAndRoles.first().accountCurrencyUomId
    if (currencyUomId) billingAccounts = BillingAccountWorker.makePartyBillingAccountList(userLogin, currencyUomId, partyId, delegator, dispatcher)
}
context.billingAccounts = billingAccounts
context.showOld = showOld
context.partyId = partyId
context.paymentMethodValueMaps = PaymentWorker.getPartyPaymentMethodValueMaps(delegator, partyId, showOld)

postalAddressForTemplate = context.postalAddress
postalAddressTemplateSuffix = context.postalAddressTemplateSuffix

if (!postalAddressTemplateSuffix) {
    postalAddressTemplateSuffix = ".ftl"
}
context.postalAddressTemplate = "PostalAddress" + postalAddressTemplateSuffix
if (postalAddressForTemplate && postalAddressForTemplate.countryGeoId) {
    postalAddressTemplate = "PostalAddress_" + postalAddressForTemplate.countryGeoId + postalAddressTemplateSuffix
    file = new File(addressTemplatePath + postalAddressTemplate)
    if (file.exists()) {
        context.postalAddressTemplate = postalAddressTemplate
    }
}
if (userLogin) {
    userLoginParty = userLogin.getRelatedOne("Party", true)
    if (userLoginParty) {
        userLoginPartyPrimaryEmails = userLoginParty.getRelated("PartyContactMechPurpose", [contactMechPurposeTypeId : "PRIMARY_EMAIL"], null, false)
        if (userLoginPartyPrimaryEmails) {
            context.thisUserPrimaryEmail = userLoginPartyPrimaryEmails.get(0)
        }
    }
}

import org.apache.ofbiz.base.util.Debug
import org.apache.ofbiz.accounting.invoice.InvoiceWorker
import org.apache.ofbiz.accounting.payment.PaymentWorker
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityTypeUtil

Boolean actualCurrency = new Boolean(context.actualCurrency)
if (actualCurrency == null) {
    actualCurrency = true
}
actualCurrencyUomId = context.actualCurrencyUomId
if (!actualCurrencyUomId) {
    actualCurrencyUomId = context.defaultOrganizationPartyCurrencyUomId
}
//get total/unapplied/applied invoices separated by sales/purch amount:
totalInvSaApplied = BigDecimal.ZERO
totalInvSaNotApplied = BigDecimal.ZERO
totalInvPuApplied = BigDecimal.ZERO
totalInvPuNotApplied = BigDecimal.ZERO

invExprs =
        EntityCondition.makeCondition([
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_IN_PROCESS"),
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_WRITEOFF"),
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"),
                EntityCondition.makeCondition([
                        EntityCondition.makeCondition([
                                EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, parameters.partyId),
                                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, context.defaultOrganizationPartyId)
                        ],EntityOperator.AND),
                        EntityCondition.makeCondition([
                                EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, context.defaultOrganizationPartyId),
                                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, parameters.partyId)
                        ],EntityOperator.AND)
                ],EntityOperator.OR)
        ],EntityOperator.AND)

invIterator = from("InvoiceAndType").where(invExprs).cursorScrollInsensitive().distinct().queryIterator()

while (invoice = invIterator.next()) {
    Boolean isPurchaseInvoice = EntityTypeUtil.hasParentType(delegator, "InvoiceType", "invoiceTypeId", invoice.getString("invoiceTypeId"), "parentTypeId", "PURCHASE_INVOICE")
    Boolean isSalesInvoice = EntityTypeUtil.hasParentType(delegator, "InvoiceType", "invoiceTypeId", (String) invoice.getString("invoiceTypeId"), "parentTypeId", "SALES_INVOICE")
    if (isPurchaseInvoice) {
        totalInvPuApplied += InvoiceWorker.getInvoiceApplied(invoice, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP)
        totalInvPuNotApplied += InvoiceWorker.getInvoiceNotApplied(invoice, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP)
    }
    else if (isSalesInvoice) {
        totalInvSaApplied += InvoiceWorker.getInvoiceApplied(invoice, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP)
        totalInvSaNotApplied += InvoiceWorker.getInvoiceNotApplied(invoice, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP)
    }
    else {
        Debug.logError("InvoiceType: " + invoice.invoiceTypeId + " without a valid parentTypeId: " + invoice.parentTypeId + " !!!! Should be either PURCHASE_INVOICE or SALES_INVOICE", "")
    }
}

invIterator.close()

//get total/unapplied/applied payment in/out total amount:
totalPayInApplied = BigDecimal.ZERO
totalPayInNotApplied = BigDecimal.ZERO
totalPayOutApplied = BigDecimal.ZERO
totalPayOutNotApplied = BigDecimal.ZERO

payExprs =
        EntityCondition.makeCondition([
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PMNT_NOTPAID"),
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PMNT_CANCELLED"),
                EntityCondition.makeCondition([
                        EntityCondition.makeCondition([
                                EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, parameters.partyId),
                                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, context.defaultOrganizationPartyId)
                        ], EntityOperator.AND),
                        EntityCondition.makeCondition([
                                EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, context.defaultOrganizationPartyId),
                                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, parameters.partyId)
                        ], EntityOperator.AND)
                ], EntityOperator.OR)
        ], EntityOperator.AND)

payIterator = from("PaymentAndType").where(payExprs).cursorScrollInsensitive().distinct().queryIterator()

while (payment = payIterator.next()) {
    if ("DISBURSEMENT".equals(payment.parentTypeId) || "TAX_PAYMENT".equals(payment.parentTypeId)) {
        totalPayOutApplied += PaymentWorker.getPaymentApplied(payment, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP)
        totalPayOutNotApplied += PaymentWorker.getPaymentNotApplied(payment, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP)
    }
    else if ("RECEIPT".equals(payment.parentTypeId)) {
        totalPayInApplied += PaymentWorker.getPaymentApplied(payment, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP)
        totalPayInNotApplied += PaymentWorker.getPaymentNotApplied(payment, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP)
    }
    else {
        Debug.logError("PaymentTypeId: " + payment.paymentTypeId + " without a valid parentTypeId: " + payment.parentTypeId + " !!!! Should be either DISBURSEMENT, TAX_PAYMENT or RECEIPT", "")
    }
}
payIterator.close()

context.finanSummary = [:]
context.finanSummary.totalSalesInvoice = totalSalesInvoice = totalInvSaApplied.add(totalInvSaNotApplied)
context.finanSummary.totalPurchaseInvoice = totalPurchaseInvoice = totalInvPuApplied.add(totalInvPuNotApplied)
context.finanSummary.totalPaymentsIn = totalPaymentsIn = totalPayInApplied.add(totalPayInNotApplied)
context.finanSummary.totalPaymentsOut = totalPaymentsOut = totalPayOutApplied.add(totalPayOutNotApplied)
context.finanSummary.totalInvoiceNotApplied = totalInvSaNotApplied.subtract(totalInvPuNotApplied)
context.finanSummary.totalPaymentNotApplied = totalPayInNotApplied.subtract(totalPayOutNotApplied)

transferAmount = totalSalesInvoice.subtract(totalPurchaseInvoice).subtract(totalPaymentsIn).subtract(totalPaymentsOut)

if (transferAmount.signum() == -1) { // negative?
    context.finanSummary.totalToBeReceived = transferAmount.negate()
} else {
    context.finanSummary.totalToBePaid = transferAmount
}

import org.apache.ofbiz.common.geo.GeoWorker
import org.apache.ofbiz.base.util.UtilMisc
import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.base.util.UtilProperties

uiLabelMap = UtilProperties.getResourceBundleMap("PartyUiLabels", locale)
uiLabelMap.addBottomResourceBundle("CommonUiLabels")

partyId = parameters.partyId ?: parameters.party_id
userLoginId = parameters.userlogin_id ?: parameters.userLoginId

if (!partyId && userLoginId) {
    thisUserLogin = from("UserLogin").where("userLoginId", userLoginId).queryOne()
    if (thisUserLogin) {
        partyId = thisUserLogin.partyId
    }
}
geoPointId = parameters.geoPointId
context.partyId = partyId

if (!geoPointId) {
    latestGeoPoint = GeoWorker.findLatestGeoPoint(delegator, "PartyAndGeoPoint", "partyId", partyId, null, null)
} else {
    latestGeoPoint = from("GeoPoint").where("geoPointId", geoPointId).queryOne()
}
if (latestGeoPoint) {
    context.latestGeoPoint = latestGeoPoint

    List geoCenter = UtilMisc.toList(UtilMisc.toMap("lat", latestGeoPoint.latitude, "lon", latestGeoPoint.longitude, "zoom", "13"))

    if (UtilValidate.isNotEmpty(latestGeoPoint) && latestGeoPoint.containsKey("latitude") && latestGeoPoint.containsKey("longitude")) {
        List geoPoints = UtilMisc.toList(UtilMisc.toMap("lat", latestGeoPoint.latitude, "lon", latestGeoPoint.longitude, "partyId", partyId,
                "link", UtilMisc.toMap("url", "viewprofile?partyId="+ partyId, "label", uiLabelMap.PartyProfile + " " + uiLabelMap.CommonOf + " " + partyId)))

        Map geoChart = UtilMisc.toMap("width", "500px", "height", "450px", "controlUI" , "small", "dataSourceId", latestGeoPoint.dataSourceId, "points", geoPoints)
        context.geoChart = geoChart
    }
    if (latestGeoPoint && latestGeoPoint.elevationUomId) {
        elevationUom = from("Uom").where("uomId", latestGeoPoint.elevationUomId).queryOne()
        context.elevationUomAbbr = elevationUom.abbreviation
    }
}

roleTypeId = parameters.roleTypeId
roleTypeAndParty = from("RoleTypeAndParty").where("partyId", parameters.partyId, "roleTypeId", roleTypeId).queryFirst()
if (roleTypeAndParty) {
    if ("ACCOUNT".equals(roleTypeId)) {
        context.accountDescription = roleTypeAndParty.description
    } else if ("CONTACT".equals(roleTypeId)) {
        context.contactDescription = roleTypeAndParty.description
    } else if ("LEAD".equals(roleTypeId)) {
        context.leadDescription = roleTypeAndParty.description
        partyRelationships = from("PartyRelationship").where("partyIdTo", parameters.partyId, "roleTypeIdFrom", "ACCOUNT_LEAD", "roleTypeIdTo", "LEAD", "partyRelationshipTypeId", "EMPLOYMENT").filterByDate().queryFirst()
        if (partyRelationships) {
            context.partyGroupId = partyRelationships.partyIdFrom
            context.partyId = parameters.partyId
        }
    } else if ("ACCOUNT_LEAD".equals(roleTypeId)) {
        context.accountLeadDescription = roleTypeAndParty.description
        partyRelationships = from("PartyRelationship").where("partyIdFrom", parameters.partyId, "roleTypeIdFrom", "ACCOUNT_LEAD", "roleTypeIdTo", "LEAD", "partyRelationshipTypeId", "EMPLOYMENT").filterByDate().queryFirst()
        if (partyRelationships) {
            context.partyGroupId = parameters.partyId
            context.partyId = partyRelationships.partyIdTo
        }
    }
}

import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator

exprList = [EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PARTY_DISABLED"),
            EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, null)]
condList = EntityCondition.makeCondition(exprList, EntityOperator.AND)
context.andCondition = EntityCondition.makeCondition([condList, EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, null)], EntityOperator.OR)


import org.apache.ofbiz.accounting.invoice.InvoiceWorker
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator

Boolean actualCurrency = new Boolean(context.actualCurrency)
if (actualCurrency == null) {
    actualCurrency = true
}


invExprs =
        EntityCondition.makeCondition([
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_IN_PROCESS"),
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_WRITEOFF"),
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"),
                EntityCondition.makeCondition([
                        EntityCondition.makeCondition([
                                EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, parameters.partyId),
                                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, context.defaultOrganizationPartyId)
                        ],EntityOperator.AND),
                        EntityCondition.makeCondition([
                                EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, context.defaultOrganizationPartyId),
                                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, parameters.partyId)
                        ],EntityOperator.AND)
                ],EntityOperator.OR)
        ],EntityOperator.AND)

invIterator = from("InvoiceAndType").where(invExprs).cursorScrollInsensitive().distinct().queryIterator()
invoiceList = []
while (invoice = invIterator.next()) {
    unAppliedAmount = InvoiceWorker.getInvoiceNotApplied(invoice, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP)
    if (unAppliedAmount.signum() == 1) {
        if (actualCurrency.equals(true)) {
            invoiceCurrencyUomId = invoice.currencyUomId
        } else {
            invoiceCurrencyUomId = context.defaultOrganizationPartyCurrencyUomId
        }
        invoiceList.add([invoiceId : invoice.invoiceId,
                         invoiceDate : invoice.invoiceDate,
                         unAppliedAmount : unAppliedAmount,
                         invoiceCurrencyUomId : invoiceCurrencyUomId,
                         amount : InvoiceWorker.getInvoiceTotal(invoice, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP),
                         invoiceTypeId : invoice.invoiceTypeId,
                         invoiceParentTypeId : invoice.parentTypeId])
    }
}
invIterator.close()

context.ListUnAppliedInvoices = invoiceList
}

invExprs =
        EntityCondition.makeCondition([
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_IN_PROCESS"),
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_WRITEOFF"),
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"),
                EntityCondition.makeCondition([
                        EntityCondition.makeCondition([
                                EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, parameters.partyId),
                                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, context.defaultOrganizationPartyId)
                        ],EntityOperator.AND),
                        EntityCondition.makeCondition([
                                EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, context.defaultOrganizationPartyId),
                                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, parameters.partyId)
                        ],EntityOperator.AND)
                ],EntityOperator.OR)
        ],EntityOperator.AND)

invIterator = from("InvoiceAndType").where(invExprs).cursorScrollInsensitive().distinct().queryIterator()
invoiceList = []
while (invoice = invIterator.next()) {
    unAppliedAmount = InvoiceWorker.getInvoiceNotApplied(invoice, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP)
    if (unAppliedAmount.signum() == 1) {
        if (actualCurrency.equals(true)) {
            invoiceCurrencyUomId = invoice.currencyUomId
        } else {
            invoiceCurrencyUomId = context.defaultOrganizationPartyCurrencyUomId
        }
        invoiceList.add([invoiceId : invoice.invoiceId,
                         invoiceDate : invoice.invoiceDate,
                         unAppliedAmount : unAppliedAmount,
                         invoiceCurrencyUomId : invoiceCurrencyUomId,
                         amount : InvoiceWorker.getInvoiceTotal(invoice, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP),
                         invoiceTypeId : invoice.invoiceTypeId,
                         invoiceParentTypeId : invoice.parentTypeId])
    }
}
invIterator.close()

context.ListUnAppliedInvoices = invoiceList

import org.apache.ofbiz.base.util.UtilDateTime

partyId = parameters.partyId ?: parameters.party_id
userLoginId = parameters.userlogin_id ?: parameters.userLoginId

if (!partyId && userLoginId) {
    thisUserLogin = from("UserLogin").where("userLoginId", userLoginId).queryOne()
    if (thisUserLogin) {
        partyId = thisUserLogin.partyId
        parameters.partyId = partyId
    }
}

context.showOld = "true".equals(parameters.SHOW_OLD)
context.partyId = partyId
context.party = from("Party").where("partyId", partyId).queryOne()
context.nowStr = UtilDateTime.nowTimestamp().toString()

import org.apache.ofbiz.entity.transaction.TransactionUtil
import org.apache.ofbiz.base.util.Debug
import org.apache.ofbiz.entity.util.EntityUtilProperties

module = "showvisits.groovy"

partyId = parameters.partyId
context.partyId = partyId

showAll = parameters.showAll ?:"false"
context.showAll = showAll

sort = parameters.sort
context.sort = sort

visitListIt = null
sortList = ["-fromDate"]
if (sort) sortList.add(0, sort)

boolean beganTransaction = false
try {
    beganTransaction = TransactionUtil.begin()

    viewIndex = Integer.valueOf(parameters.VIEW_INDEX  ?: 1)
    viewSize = parameters.VIEW_SIZE ?Integer.valueOf(parameters.VIEW_SIZE): modelTheme.getDefaultViewSize()?:20
    context.viewIndex = viewIndex
    context.viewSize = viewSize

    // get the indexes for the partial list
    lowIndex = (((viewIndex - 1) * viewSize) + 1)
    highIndex = viewIndex * viewSize

    if (partyId) {
        visitListIt = from("Visit").where("partyId", partyId).orderBy(sortList).cursorScrollInsensitive().maxRows(highIndex).distinct().queryIterator()
    } else if (showAll.equalsIgnoreCase("true")) {
        visitListIt = from("Visit").orderBy(sortList).cursorScrollInsensitive().maxRows(highIndex).distinct().queryIterator()
    } else {
        // show active visits
        visitListIt = from("Visit").where("thruDate", null).orderBy(sortList).cursorScrollInsensitive().maxRows(highIndex).distinct().queryIterator()
    }

    // get the partial list for this page
    visitList = visitListIt.getPartialList(lowIndex, viewSize)
    if (!visitList) {
        visitList = new ArrayList()
    }

    visitListSize = visitListIt.getResultsSizeAfterPartialList()
    if (highIndex > visitListSize) {
        highIndex = visitListSize
    }
    context.visitSize = visitListSize

} catch (Exception e) {
    String errMsg = "Failure in operation, rolling back transaction"
    Debug.logError(e, errMsg, module)
    try {
        // only rollback the transaction if we started one...
        TransactionUtil.rollback(beganTransaction, errMsg, e)
    } catch (Exception e2) {
        Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), module)
    }
    // after rolling back, rethrow the exception
    throw e
} finally {
    // only commit the transaction if we started one... this will throw an exception if it fails
    visitListIt.close()
    TransactionUtil.commit(beganTransaction)
}

context.visitList = visitList
listSize = 0
if (visitList) {
    listSize = lowIndex + visitList.size()
}

if (listSize < highIndex) {
    highIndex = listSize
}
context.lowIndex = lowIndex
context.highIndex = highIndex
context.listSize = listSize

partyId = parameters.partyId
visitId = parameters.visitId

visit = null
serverHits = null
if (visitId) {
    visit = from("Visit").where("visitId", visitId).queryOne()
    if (visit) {
        serverHits = from("ServerHit").where("visitId", visitId).orderBy("-hitStartDateTime").queryList()
    }
}

viewIndex = 0
try {
    viewIndex = Integer.valueOf((String) parameters.VIEW_INDEX).intValue()
} catch (Exception e) {
    viewIndex = 0
}

viewSize = 20
try {
    viewSize = Integer.valueOf((String) parameters.VIEW_SIZE).intValue()
} catch (Exception e) {
    viewSize = 20
}

listSize = 0
if (serverHits) {
    listSize = serverHits.size()
}
lowIndex = viewIndex * viewSize
highIndex = (viewIndex + 1) * viewSize
if (listSize < highIndex) {
    highIndex = listSize
}

context.partyId = partyId
context.visitId = visitId
context.visit = visit
context.serverHits = serverHits

context.viewIndex = viewIndex
context.viewSize = viewSize
context.listSize = listSize
context.lowIndex = lowIndex
context.highIndex = highIndex

// standard partymgr permissions
context.hasViewPermission = security.hasEntityPermission("PARTYMGR", "_VIEW", session)
context.hasCreatePermission = security.hasEntityPermission("PARTYMGR", "_CREATE", session)
context.hasUpdatePermission = security.hasEntityPermission("PARTYMGR", "_UPDATE", session)
context.hasDeletePermission = security.hasEntityPermission("PARTYMGR", "_DELETE", session)
// extended pay_info permissions
context.hasPayInfoPermission = security.hasEntityPermission("PAY_INFO", "_VIEW", session) || security.hasEntityPermission("ACCOUNTING", "_VIEW", session)
// extended pcm (party contact mechanism) permissions
context.hasPcmCreatePermission = security.hasEntityPermission("PARTYMGR_PCM", "_CREATE", session)
context.hasPcmUpdatePermission = security.hasEntityPermission("PARTYMGR_PCM", "_UPDATE", session)