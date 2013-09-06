package com.adobe.acs.commons.forms.helpers.impl;

import com.adobe.acs.commons.forms.Form;
import com.adobe.acs.commons.forms.helpers.FormHelper;
import com.adobe.acs.commons.forms.helpers.ForwardAsGetFormHelper;
import com.adobe.acs.commons.forms.helpers.impl.synthetics.SyntheticSlingHttpServletGetRequest;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONException;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;

@Component(label = "ACS AEM Commons - Forward Form Manager", description = "Internal Forward-as-GET Form Helper", enabled = true, metatype = true, immediate = false, inherit = true)
@Properties({ @Property(label = "Vendor", name = Constants.SERVICE_VENDOR, value = "ACS", propertyPrivate = true) })
@Service( value = { FormHelper.class, ForwardAsGetFormHelper.class })
public class ForwardAsGetFormHelperImpl extends PostFormHelperImpl implements ForwardAsGetFormHelper {
    private static final Logger log = LoggerFactory.getLogger(ForwardAsGetFormHelperImpl.class);
    private static final String CQ_PAGE_RESOURCE_TYPE = "cq/Page";

    @Override
    public Form getForm(final String formName, final SlingHttpServletRequest request) {
        if(this.doHandlePost(formName, request)) {
            // Read the request from the POST parameters
            return this.getPostForm(formName, request);
        } else {
            final String key = this.getLookupKey(formName);
            final Object obj = request.getAttribute(key);
            if (obj instanceof Form) {
                return this.getProtectedForm((Form) obj);
            } else {
                log.info("Unable to find Form in Request attribute: [ {} => {} ]", key, obj);
                return new Form(formName, request.getResource().getPath());
            }
        }
    }

    @Override
    public void forwardAsGet(final Form form, final Page page,
                             final SlingHttpServletRequest request,
                             final SlingHttpServletResponse response) throws ServletException, IOException {

        final RequestDispatcherOptions options = new RequestDispatcherOptions();

        options.setReplaceSelectors("");
        options.setForceResourceType(CQ_PAGE_RESOURCE_TYPE);

        this.forwardAsGet(form, page, request, response, options);
    }

    @Override
    public void forwardAsGet(final Form form, final Page page,
                             final SlingHttpServletRequest request,
                             final SlingHttpServletResponse response,
                             final RequestDispatcherOptions options) throws ServletException, IOException {

        options.setForceResourceType(CQ_PAGE_RESOURCE_TYPE);

        final String path = page.getPath() + FormHelper.EXTENSION;

        this.forwardAsGet(form, path, request, response, options);
    }


    @Override
    public void forwardAsGet(final Form form, final Resource resource,
                             final SlingHttpServletRequest request,
                             final SlingHttpServletResponse response) throws ServletException, IOException {

        final RequestDispatcherOptions options = new RequestDispatcherOptions();

        this.forwardAsGet(form, resource, request, response, options);
    }

    @Override
    public void forwardAsGet(final Form form, final Resource resource,
                             final SlingHttpServletRequest request,
                             final SlingHttpServletResponse response,
                             final RequestDispatcherOptions options) throws ServletException, IOException {

        final String path = resource.getPath();

        this.forwardAsGet(form, path, request, response, options);
    }

    @Override
    public void forwardAsGet(final Form form, final String path,
                             final SlingHttpServletRequest request,
                             final SlingHttpServletResponse response,
                             final RequestDispatcherOptions options) throws ServletException, IOException {

        this.setRequestAttrForm(request, form);

        final SlingHttpServletRequest syntheticRequest = new SyntheticSlingHttpServletGetRequest(request);

        if(log.isDebugEnabled()) {
            log.debug("Forwarding as GET to path: {} ", path);
            log.debug("Forwarding as GET w/ replace selectors: {} ", options.getReplaceSelectors());
            log.debug("Forwarding as GET w/ add selectors: {} ", options.getAddSelectors());
            log.debug("Forwarding as GET w/ suffix: {} ", options.getReplaceSuffix());
            log.debug("Forwarding as GET w/ forced resourceType: {} ", options.getForceResourceType());
        }

        request.getRequestDispatcher(path, options).forward(syntheticRequest, response);
    }

    @Override
    public void renderForm(final Form form, final Page page, final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException, ServletException, JSONException {
        final String formSelector = this.getFormSelector(request);

        this.renderOtherForm(form, page, formSelector, request, response);    }

    @Override
    public void renderForm(final Form form, final Resource resource, final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException, ServletException, JSONException {
        final String formSelector = this.getFormSelector(request);

        this.renderOtherForm(form, resource, formSelector, request, response);
    }

    @Override
    public void renderForm(final Form form, final String path, final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException, ServletException, JSONException {
        final String formSelector = this.getFormSelector(request);

        this.renderOtherForm(form, path, formSelector, request, response);
    }

    @Override
    public void renderOtherForm(final Form form, final String path, final String formSelector, final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException, ServletException, JSONException {
        final RequestDispatcherOptions options = new RequestDispatcherOptions();

        if(StringUtils.isNotBlank(formSelector)) {
            options.setReplaceSelectors(formSelector);
        }

        this.forwardAsGet(form, path, request, response, options);
    }

    @Override
    public void renderOtherForm(final Form form, final Page page, final String formSelector, final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException, ServletException, JSONException {
        final RequestDispatcherOptions options = new RequestDispatcherOptions();

        if(StringUtils.isNotBlank(formSelector)) {
            options.setReplaceSelectors(formSelector);
        }

        this.forwardAsGet(form, page, request, response, options);
    }

    @Override
    public void renderOtherForm(final Form form, final Resource resource, final String formSelector, final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException, ServletException, JSONException {
        final RequestDispatcherOptions options = new RequestDispatcherOptions();

        if(StringUtils.isNotBlank(formSelector)) {
            options.setReplaceSelectors(formSelector);
        }

        this.forwardAsGet(form, resource, request, response, options);
    }


    /**
     * Gets the Key used to look up the Form from the Request Attributes used to transport Forward-as-GET Forrms
     *
     * @param formName
     * @return
     */
    protected String getLookupKey(final String formName) {
        return REQUEST_ATTR_FORM_KEY + formName;
    }

    /**
     * Persists the Form obj to the Request via Request Attribute
     *
     * @param request
     * @param form
     */
    protected void setRequestAttrForm(final SlingHttpServletRequest request,
                                    final Form form) {
        final String key = this.getLookupKey(form.getName());
        request.setAttribute(key, form);
    }
}