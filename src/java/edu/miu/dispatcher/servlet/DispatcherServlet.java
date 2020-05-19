package edu.miu.dispatcher.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.miu.dispatcher.annotation.Controller;
import edu.miu.dispatcher.annotation.RequestMapping;

@WebServlet(name = "MyOwnServlet", description = "This is my first annotated servlet", urlPatterns = "/", initParams = {
		@WebInitParam(name = "controllerPath", value = "edu.miu.controller"), }, loadOnStartup = 1)
public class DispatcherServlet extends HttpServlet {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7321614954120217240L;
	Map<String, RequestMappingDetail> requestMappings = new HashMap<String, RequestMappingDetail>();

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String path = request.getRequestURI().substring(request.getContextPath().length());
		String httpMethod = request.getMethod();

		if (requestMappings.containsKey(path)) {
			RequestMappingDetail rmd = requestMappings.get(path);
			if (rmd.getHttpMethod().equalsIgnoreCase(httpMethod)) {
				Method executor = rmd.getExecutor();
				Class[] parameterTypes = executor.getParameterTypes();
				Object[] parameterValues = new Object[parameterTypes.length];
				for (int i = 0; i < parameterTypes.length; i++) {
					if (parameterTypes[i].getName().equals(HttpServletRequest.class.getName())) {
						parameterValues[i] = request;
					}
					if (parameterTypes[i].getName().equals(HttpServletResponse.class.getName())) {
						parameterValues[i] = response;
					}
				}

				try {
					String result = (String) executor.invoke(rmd.getController(), parameterValues);
					RequestDispatcher rd = request.getRequestDispatcher(result);
					rd.forward(request, response);
					return;
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				response.sendError(405, "HTTP Status 405 – Method Not Allowed");
			}
		} else {
			response.sendError(404, "HTTP Status 404 – Not Found");
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		System.out.println("Scanning using Reflections:");

		try {
			List<Class> allClasses = getClasses("edu.miu.controller");
			for (Class e : allClasses) {
				Annotation annotation = e.getAnnotation(Controller.class);
				if (annotation != null) {
					Object instance = e.getDeclaredConstructor().newInstance();
					Controller controller = (Controller) annotation;
					Method[] methods = e.getDeclaredMethods();
					for (Method m : methods) {
						RequestMapping rm = m.getAnnotation(RequestMapping.class);
						System.out.println("Request Mapping: Path = " + rm.path() + ", Method = " + rm.method()
								+ ", Executor = " + m.getName() + ", Controller = " + e.getName());
						requestMappings.put(rm.path(), new RequestMappingDetail(instance, m, rm.method()));
					}
				}
			}
		} catch (ClassNotFoundException | IOException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		super.destroy();
	}

	private List<Class> getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class> classes = new ArrayList<>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	private List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(
						Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	private class RequestMappingDetail {
		private Object controller;
		private Method executor;
		private String httpMethod;

		public RequestMappingDetail(Object controller, Method executor, String httpMethod) {
			super();
			this.controller = controller;
			this.executor = executor;
			this.httpMethod = httpMethod;
		}

		public Object getController() {
			return controller;
		}

		public Method getExecutor() {
			return executor;
		}

		public String getHttpMethod() {
			return httpMethod;
		}
	}

}
