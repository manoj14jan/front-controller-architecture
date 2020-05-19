package edu.miu.controller;

import javax.servlet.http.HttpServletRequest;

import edu.miu.dispatcher.annotation.Controller;
import edu.miu.dispatcher.annotation.RequestMapping;

@Controller
public class CalculatorController {

	@RequestMapping(method = "get", path = "/")
	public String index() {
		return "index.jsp";
	}

	@RequestMapping(method = "get", path = "/calculator")
	public String calculator() {
		return "calculator.jsp";
	}

	@RequestMapping(method = "post", path = "/calculate")
	public String calculate(HttpServletRequest request) {
		String sum = "";
		String product = "";

		String add1 = request.getParameter("add1").trim();
		String add2 = request.getParameter("add2").trim();
		String mult1 = request.getParameter("mult1").trim();
		String mult2 = request.getParameter("mult2").trim();

		// Check for valid inputs....
		try {
			Integer a1 = Integer.parseInt(add1);
			Integer a2 = Integer.parseInt(add2);
			sum = "" + (a1 + a2);
		} catch (NumberFormatException e) {
			request.setAttribute("msg", "Number Format Exception. Enter valid number");
			return "calculator.jsp";
		}

		/*
		 * Integer.parseInt(str) throws NumberFormatException if the string cannot be
		 * converted to an integer.
		 */

		try {
			Integer m1 = Integer.parseInt(mult1);
			Integer m2 = Integer.parseInt(mult2);
			product = "" + (m1 * m2);
		} catch (NumberFormatException e) {
			request.setAttribute("msg", "Number Format Exception. Enter valid number");
			return "calculator.jsp";
		}

		request.setAttribute("sum", sum);
		request.setAttribute("product", product);
		return "result.jsp";
	}

}
