import { Navigate, Route, Routes } from "react-router-dom";
import CreateSurveyPage from "./features/admin/pages/CreateSurveyPage.tsx";
import LoginPage from "./features/auth/pages/LoginPage.tsx";
import AdminRoute from "./features/auth/AdminRoute.tsx";
import ProtectedRoute from "./features/auth/ProtectedRoute.tsx";
import PublicOnlyRoute from "./features/auth/PublicOnlyRoute.tsx";
import RoleRoute from "./features/auth/RoleRoute.tsx";
import { useAuth } from "./features/auth/useAuth";
import { getDefaultAppRoute } from "./features/auth/defaultRoute";
import RegisterPage from "./features/auth/pages/RegisterPage.tsx";
import UploadDocumentsPage from "./features/auth/pages/UploadDocumentsPage.tsx";
import VerifyEmailPage from "./features/auth/pages/VerifyEmailPage.tsx";
import PendingStudentsPage from "./features/admin/pages/PendingStudentsPage.tsx";
import SurveysDetailPage from "./features/surveys/pages/SurveyDetailPage.tsx";
import SurveysListPage from "./features/surveys/pages/SurveysPage.tsx";
import SurveyResultDetailPage from "./features/survey-results/pages/SurveyResultDetailPage.tsx";
import SurveyResultsPage from "./features/survey-results/pages/SurveyResultsPage.tsx";

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
              <Route path="/verify-email" element={<VerifyEmailPage />} />
          </Route>
          <Route element={<ProtectedRoute />}>
              <Route path="/upload-documents" element={<UploadDocumentsPage />} />
              <Route path="/surveys" element={<SurveysListPage />} />
              <Route path="/surveys/:id" element={<SurveysDetailPage />} />
              <Route element={<RoleRoute allowedRoles={["ADMIN", "TEACHER"]} />}>
                  <Route path="/survey-results" element={<SurveyResultsPage />} />
                  <Route path="/survey-results/:id" element={<SurveyResultDetailPage />} />
              </Route>
              <Route element={<AdminRoute />}>
                  <Route path="/admin/students/pending" element={<PendingStudentsPage />} />
                  <Route path="/admin/surveys/create" element={<CreateSurveyPage />} />
              </Route>
          </Route>
          <Route path="*" element={<RootRedirect />} />
      </Routes>
  );
}

export default App
