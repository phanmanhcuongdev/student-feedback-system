import { Navigate, Route, Routes } from "react-router-dom";
import AppShell from "./components/layout/AppShell";
import CreateSurveyPage from "./features/admin/pages/CreateSurveyPage.tsx";
import AdminSurveysPage from "./features/admin/pages/AdminSurveysPage.tsx";
import LoginPage from "./features/auth/pages/LoginPage.tsx";
import AdminRoute from "./features/auth/AdminRoute.tsx";
import ProtectedRoute from "./features/auth/ProtectedRoute.tsx";
import PublicOnlyRoute from "./features/auth/PublicOnlyRoute.tsx";
import RoleRoute from "./features/auth/RoleRoute.tsx";
import { useAuth } from "./features/auth/useAuth";
import { getDefaultAppRoute } from "./features/auth/defaultRoute";
import RegisterPage from "./features/auth/pages/RegisterPage.tsx";
import ForgotPasswordPage from "./features/auth/pages/ForgotPasswordPage.tsx";
import ResetPasswordPage from "./features/auth/pages/ResetPasswordPage.tsx";
import ChangePasswordPage from "./features/auth/pages/ChangePasswordPage.tsx";
import UploadDocumentsPage from "./features/auth/pages/UploadDocumentsPage.tsx";
import VerifyEmailPage from "./features/auth/pages/VerifyEmailPage.tsx";
import AccountOverviewPage from "./features/account/pages/AccountOverviewPage.tsx";
import AccountLayout from "./features/account/layouts/AccountLayout.tsx";
import PendingStudentsPage from "./features/admin/pages/PendingStudentsPage.tsx";
import UsersPage from "./features/admin/pages/UsersPage.tsx";
import UserDetailPage from "./features/admin/pages/UserDetailPage.tsx";
import SurveysDetailPage from "./features/surveys/pages/SurveyDetailPage.tsx";
import SurveysListPage from "./features/surveys/pages/SurveysPage.tsx";
import SurveyResultDetailPage from "./features/survey-results/pages/SurveyResultDetailPage.tsx";
import SurveyResultsPage from "./features/survey-results/pages/SurveyResultsPage.tsx";
import NotificationsPage from "./features/notifications/pages/NotificationsPage.tsx";
import FeedbackPage from "./features/feedback/pages/FeedbackPage.tsx";
import ManageFeedbackPage from "./features/feedback/pages/ManageFeedbackPage.tsx";
import StudentDashboardPage from "./features/dashboard/pages/StudentDashboardPage.tsx";
import LecturerDashboardPage from "./features/dashboard/pages/LecturerDashboardPage.tsx";
import AdminDashboardPage from "./features/dashboard/pages/AdminDashboardPage.tsx";

function RootRedirect() {
    const { isAuthenticated, session } = useAuth();
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return <Navigate to={getDefaultAppRoute(session?.role, session?.studentStatus)} replace />;
}

function App() {
  return (
      <Routes>
          <Route path="/" element={<RootRedirect />} />
          <Route element={<PublicOnlyRoute />}>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/forgot-password" element={<ForgotPasswordPage />} />
              <Route path="/reset-password" element={<ResetPasswordPage />} />
              <Route path="/verify-email" element={<VerifyEmailPage />} />
          </Route>
          <Route element={<ProtectedRoute />}>
              <Route path="/upload-documents" element={<UploadDocumentsPage />} />
              <Route path="/change-password" element={<Navigate to="/account/security" replace />} />
              <Route element={<AppShell />}>
                  <Route path="/account" element={<AccountLayout />}>
                      <Route index element={<AccountOverviewPage />} />
                      <Route path="security" element={<ChangePasswordPage />} />
                  </Route>
                  <Route path="/dashboard/student" element={<StudentDashboardPage />} />
                  <Route path="/notifications" element={<NotificationsPage />} />
                  <Route path="/feedback" element={<FeedbackPage />} />
                  <Route path="/surveys" element={<SurveysListPage />} />
                  <Route path="/surveys/:id" element={<SurveysDetailPage />} />
                  <Route element={<RoleRoute allowedRoles={["ADMIN", "TEACHER"]} />}>
                      <Route path="/dashboard/lecturer" element={<LecturerDashboardPage />} />
                      <Route path="/survey-results" element={<SurveyResultsPage />} />
                      <Route path="/survey-results/:id" element={<SurveyResultDetailPage />} />
                      <Route path="/feedback/manage" element={<ManageFeedbackPage />} />
                  </Route>
                  <Route element={<AdminRoute />}>
                      <Route path="/dashboard/admin" element={<AdminDashboardPage />} />
                      <Route path="/admin/users" element={<UsersPage />} />
                      <Route path="/admin/users/:id" element={<UserDetailPage />} />
                      <Route path="/admin/surveys" element={<AdminSurveysPage />} />
                      <Route path="/admin/students/pending" element={<PendingStudentsPage />} />
                      <Route path="/admin/surveys/create" element={<CreateSurveyPage />} />
                      <Route path="/admin/surveys/:id/edit" element={<CreateSurveyPage />} />
                  </Route>
              </Route>
          </Route>
          <Route path="*" element={<RootRedirect />} />
      </Routes>
  );
}

export default App
