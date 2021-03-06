<%@ include file="/WEB-INF/jsp/include.jsp" %>

<h1 class="portal-logo search-launcher" style="text-align:right;float:right;vertical-align:middle;">
  <div id="webSearchContainer" class="fl-widget">
    <div class="fl-widget-inner">
      <div class="fl-widget-content">
        <c:set var="searchLabel"><spring:message code="search"/></c:set>
        <form method="post" action="${launchUrl}" id="webSearchForm" class="form-search">
          <input id="webSearchInput" class="input-large search-query" value="" name="query" type="text" />
          <input id="webSearchSubmit" type="submit" name="submit" class="btn" value="${searchLabel}" />
        </form>
      </div>
    </div>
  </div>
</h1>