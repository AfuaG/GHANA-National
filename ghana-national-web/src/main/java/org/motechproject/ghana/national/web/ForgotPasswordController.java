package org.motechproject.ghana.national.web;

import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.service.StaffService;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/forgotPassword")
public class ForgotPasswordController {
    private StaffService staffService;

    public ForgotPasswordController() {
    }

    @Autowired
    public ForgotPasswordController(StaffService staffService) {
        this.staffService = staffService;
    }

    @LoginAsAdmin
    @ApiSession
    @RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
    public ModelAndView changePasswordAndSendEmail(HttpServletRequest request) throws Exception {

        ModelAndView modelAndView = new ModelAndView("redirect:/login.jsp");
        ModelMap modelMap = modelAndView.getModelMap();
        String emailId = request.getParameter("emailId");
        int status = Constants.EMAIL_USER_NOT_FOUND;
        if(!isInvalidEmailId(emailId)) {
            status = staffService.changePasswordByEmailId(emailId);
        }

        switch (status) {
            case Constants.EMAIL_SUCCESS:
                modelMap.put(Constants.FORGOT_PASSWORD_MESSAGE, Constants.FORGOT_PASSWORD_SUCCESS);
                break;
            case Constants.EMAIL_FAILURE:
                modelMap.put(Constants.FORGOT_PASSWORD_MESSAGE, Constants.FORGOT_PASSWORD_FAILURE);
                break;
            case Constants.EMAIL_USER_NOT_FOUND:
                modelMap.put(Constants.FORGOT_PASSWORD_MESSAGE, Constants.FORGOT_PASSWORD_USER_NOT_FOUND);
                break;
        }
        modelAndView.addAllObjects(modelMap);
        return modelAndView;
    }

    private boolean isInvalidEmailId(String emailId) {
        return (emailId.isEmpty() || emailId.equals("admin"));
    }
}
