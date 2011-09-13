Admin Login

Meta:
@category smoke

Narrative: 

As an Super Admin User I want to Login/Logout of the Web application So that I can access the application.

Scenario: Successful Login

Given the admin user logs in with password admin
Then the admin dashboard page should be displayed

Scenario: Failed Login

Given the admin user logs in with password foo
Then the admin dashboard page should be displayed with an error

