package org.fastcatsearch.http.action.management.common;

import java.io.Writer;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/common/modules-running-state", authority = ActionAuthority.Servers, authorityLevel = ActionAuthorityLevel.NONE)
public class GetModuleStateAction extends AuthAction {
	
	@SuppressWarnings("unchecked")
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		try {
			
			ServiceManager serviceManager = ServiceManager.getInstance();
			
			AbstractService service = null;
			
			Writer writer = response.getWriter();
			
			String[] classNames = request.getParameter("services","").split(",");
			ResponseWriter resultWriter = getDefaultResponseWriter(writer);
			resultWriter.object().key("moduleState").array();
			for(String className : classNames) {
				@SuppressWarnings("rawtypes")
				Class cls = Class.forName(className.trim());
				String[] fqdn = cls.getName().split("[.]");
				String serviceName = fqdn[fqdn.length-1];
				service = serviceManager.getService(cls);
				resultWriter.object()
					.key("serviceName").value(serviceName)
					.key("serviceClass").value(className)
					.key("status").value(service.isRunning())
					.endObject();
			}
			resultWriter .endArray()
			.endObject()
			.done();
		} catch (Exception e) {
			logger.error("", e);
		}
	}
}
