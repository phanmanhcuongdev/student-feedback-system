import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./useAuth";
import { getDefaultAppRoute } from "./defaultRoute";

export default function AdminRoute() {
    const { session } = useAuth();

    if (!session) {
        return <Navigate to="/login" replace />;
    }

    if (session.role !== "ADMIN") {
        return <Navigate to={getDefaultAppRoute(session.role, session.studentStatus)} replace />;
    }

    return <Outlet />;
}
