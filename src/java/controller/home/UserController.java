/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.home;

import dal.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Account;
import configs.EncodeData;
import configs.Validate;
import dal.AppointmentDAO;
import dal.PatientDao;
import dal.ReservationDAO;
import jakarta.servlet.http.Part;
import java.util.List;
import model.Appointment;
import model.Reservation;
import model.Role;

/**
 *
 * @author doans
 */
public class UserController extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        response.setContentType("text/html;charset=UTF-8");
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        UserDAO userdao = new UserDAO();
        Account user = (Account) session.getAttribute("user");
        String alert = null;
        String message = null;

        try ( PrintWriter out = response.getWriter()) {
            if (action.equals("login")) {
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }
            if (action.equals("checklogin")) {
                String email = request.getParameter("email");
                String password = request.getParameter("password");
                String remember = request.getParameter("remember");
                String enpassword = EncodeData.enCode(password);
                Account account = userdao.login(email, enpassword);
                if (account == null) {
                    request.setAttribute("error", "Email hoặc mật khẩu không chính xác!");
                    request.getRequestDispatcher("user?action=login").forward(request, response);
                } else if (account.isStatus() == false) {
                    request.setAttribute("error", "Tài khoản đã bị khóa !");
                    request.getRequestDispatcher("user?action=login").forward(request, response);
                } else {
                    session.setAttribute("user", account);
                    Cookie cemail = new Cookie("email", email);
                    Cookie cpass = new Cookie("pass", password);
                    Cookie rem = new Cookie("remember", remember);
                    if (remember != null) {
                        cemail.setMaxAge(60 * 60 * 24 * 30);
                        cpass.setMaxAge(60 * 60 * 24 * 3);
                        rem.setMaxAge(60 * 60 * 24 * 30);
                    } else {
                        cemail.setMaxAge(0);
                        cpass.setMaxAge(0);
                        rem.setMaxAge(0);
                    }
                    response.addCookie(cemail);
                    response.addCookie(cpass);
                    response.addCookie(rem);
                    if (account.getRole().getRole_id() == 1) {
                        response.sendRedirect("dashboard?action=default");
                    } else {
                        response.sendRedirect("home");
                    }
                }
            }

            if (action.equals("profile")) {
                request.getRequestDispatcher("profile.jsp").forward(request, response);
            }

            if (action.equals("logout")) {
                session.invalidate();
                response.sendRedirect("home");
                return;
            }

            // Register
            if (action.equals("register")) {
                request.getRequestDispatcher("register.jsp").forward(request, response);
            }
            if (action.equals("checkregister")) {
                String email = request.getParameter("email");
                String password = request.getParameter("password");
                String repassword = request.getParameter("repassword");
                String username = request.getParameter("username");
                String name = request.getParameter("name");
                String rgender = request.getParameter("gender");
                String rphone = request.getParameter("phone");
                int role_id = 2;
                String img = "default";
                boolean status = true;
                String enpassword = EncodeData.enCode(password);
                boolean gender = Boolean.parseBoolean(rgender);
                int phone = Integer.parseInt(rphone);
                String fullname = Validate.capitalizeFirstLetter(name);
                Account account = userdao.checkAcc(email, username);
                if (account != null) {
                    request.setAttribute("email", email);
                    request.setAttribute("password", password);
                    request.setAttribute("repassword", repassword);
                    request.setAttribute("username", username);
                    request.setAttribute("name", name);
                    request.setAttribute("gender", rgender.equals("true"));
                    request.setAttribute("phone", rphone);
                    request.setAttribute("error", "Email hoặc username đã tồn tại trên hệ thống!");
                    request.getRequestDispatcher("user?action=register").forward(request, response);
                } else {
                    Role r = new Role(role_id);
                    userdao.Register(email, EncodeData.enCode(password), username, role_id, name, phone, gender, status);
                    Account a = new Account(username, r, EncodeData.enCode(enpassword), fullname, gender, phone, email, img, status);

                    session.setAttribute("register", a);
                    request.getRequestDispatcher("user?action=login").forward(request, response);
                }
            }
            
                        if (action.equals("updateprofile")) {
                String username = request.getParameter("username");
                String name = request.getParameter("name");
                int phone = Integer.parseInt(request.getParameter("phone"));
                boolean gender = Boolean.parseBoolean(request.getParameter("gender"));
                userdao.UpdateProfile(username, name, phone, gender);
                Account a = new Account(username, user.getRole(), name, gender, phone, user.getEmail(), user.getImg(), user.isStatus());
                session.setAttribute("user", a);
                request.setAttribute("updatesuccess", "Thông tin đã được cập nhật!");
                response.sendRedirect("user?action=profile");
            }

            if (action.equals("update_image")) {
                String username = user.getUsername();
                Part image = request.getPart("image");
                if (image != null) {
                    try {
                        Account acc = userdao.getAccountByUsername(username);
                        userdao.UpdateImage(username, image);
                        session.setAttribute("user", acc);
                    } catch (Exception e) {
                    }
                }
                alert = "success";
                message = "Cập nhật ảnh thành công";
                request.setAttribute("alert", alert);
                request.setAttribute("message", message);
                request.getRequestDispatcher("user?action=profile").forward(request, response);
            }
            
             if (action.equals("history")) {
                String type = request.getParameter("type");
                PatientDao pdao = new PatientDao();
                AppointmentDAO adao = new AppointmentDAO();
                ReservationDAO rdao = new ReservationDAO();
                List<Appointment> appointmentlist = null;
                List<Reservation> reservationlist = null;
                if (type.equals("appointment")) {
                    appointmentlist = adao.getAppointmentHistory(pdao.getPatientIDByUsername(user.getUsername()));
                } else if (type.equals("reservation")) {
                    reservationlist = rdao.getReservationListHistory(pdao.getPatientIDByUsername(user.getUsername()));
                }
                if (appointmentlist != null || reservationlist != null) {
                    int page, numperpage = 8;
                    int size = 0;
                    if (appointmentlist != null) {
                        size = appointmentlist.size();
                    } else {
                        size = reservationlist.size();
                    }
                    int num = (size % 8 == 0 ? (size / 8) : ((size / 8)) + 1);
                    String xpage = request.getParameter("page");
                    if (xpage == null) {
                        page = 1;
                    } else {
                        page = Integer.parseInt(xpage);
                    }
                    int start, end;
                    String url = null;
                    start = (page - 1) * numperpage;
                    end = Math.min(page * numperpage, size);
                    if (appointmentlist != null) {
                        appointmentlist = adao.getListByPage(appointmentlist, start, end);
                        request.setAttribute("appointmentlist", appointmentlist);
                        url = "user?action=history&type=appointment";
                    } else {
                        reservationlist = rdao.getListByPage(reservationlist, start, end);
                        request.setAttribute("reservationlist", reservationlist);
                        url = "user?action=history&type=reservation";
                    }
                    request.setAttribute("page", page);
                    request.setAttribute("url", url);
                    request.setAttribute("num", num);
                    request.getRequestDispatcher("history.jsp").forward(request, response);
                }
            }
            
            if (action.equals("changepassword")) {
                String oldpassword = EncodeData.enCode(request.getParameter("oldpassword"));
                String newpassword = request.getParameter("newpassword");
                String renewpassword = request.getParameter("renewpassword");
                if (!oldpassword.equals(user.getPassword())) {
                    request.setAttribute("oldpassword", EncodeData.deCode(oldpassword));
                    request.setAttribute("newpassword", newpassword);
                    request.setAttribute("renewpassword", renewpassword);
                    request.setAttribute("passerror", "Mật khẩu cũ không đúng!");
                    request.getRequestDispatcher("user?action=profile").forward(request, response);
                } else {
                    newpassword = EncodeData.enCode(newpassword);
                    userdao.Recover(user.getUsername(), newpassword);
                    request.setAttribute("success", "Thay đổi mật khẩu thành công, mời bạn đăng nhập lại!");
                    request.getRequestDispatcher("user?action=login").forward(request, response);
                }
            }
        } catch (IOException | SQLException | ServletException e) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
