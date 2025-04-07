package com.data7.instdesign.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

import java.io.PrintWriter;

@Log4j2
public class JSFunc {
	public static void alertLocation(String msg, String url, HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html;charset=UTF-8");
			String code = "<script>"
					+ "alert('" + msg + "');"
					+ "location.href='" + url + "';"
					+ "</script>";
			writer.print(code);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public static void back(HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html;charset=UTF-8");
			String code = "<script>"
					+ "history.back();"
					+ "</script>";
			writer.print(code);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public static void alertBack(String msg, HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html;charset=UTF-8");
			String code = "<script>"
					+ "alert('" + msg + "');"
					+ "history.back();"
					+ "</script>";
			writer.print(code);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public static void alert(String msg, HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html;charset=UTF-8");
			String code = "<script>"
					+ "alert('" + msg + "');"
					+ "</script>";
			writer.print(code);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public static void alertAndRedirect(String msg, String redirectUrl, HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html;charset=UTF-8");
			String script = "<script>"
					+ "alert('" + msg + "');"
					+ "window.location.href='" + redirectUrl + "';"
					+ "</script>";
			writer.print(script);
			writer.flush();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

    // 팝업 닫는용
    public static void alertAndClose(String msg, HttpServletResponse response) {
        try {
            PrintWriter writer = response.getWriter();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=UTF-8");
            String script = "<script>"
                    + "alert('" + escapeJavaScript(msg) + "');"
                    + "window.close();"
                    + "</script>";
            writer.print(script);
            writer.flush();
        } catch (Exception e) {
            log.error("alertAndClose 오류: {}", e.getMessage());
        }
    }

    private static String escapeJavaScript(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

	public static void alertAndRedirectWithDeleteCookie(String msg,String redirectUrl, HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html;charset=UTF-8");
			String script = "<script>"
					+ "alert('" + msg + "');"
					+ "window.location.href='" + redirectUrl + "';"
					+ "localStorage.removeItem('token');"
					+ "localStorage.removeItem('user');"
					+ "document.cookie = 'Authorization=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT;';"
					+ "</script>";
			writer.print(script);
			writer.flush();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}


}
