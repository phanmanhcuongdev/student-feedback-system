import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "./useAuth";
import { getDefaultAppRoute } from "./defaultRoute";

export default function ProtectedRoute() {
    const { isAuthenticated, session } = useAuth();
    const location = useLocation();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
    }

    if (
        session?.role === "STUDENT"
        && session.studentStatus === "EMAIL_VERIFIED"
        && location.pathname !== "/upload-documents"
    ) {
        return <Navigate to={getDefaultAppRoute(session.role, session.studentStatus)} replace />;
    }

    return <Outlet />;
}
