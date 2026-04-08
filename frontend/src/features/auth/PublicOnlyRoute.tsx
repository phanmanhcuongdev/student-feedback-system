import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./useAuth";
import { getDefaultAppRoute } from "./defaultRoute";

export default function PublicOnlyRoute() {
    const { isAuthenticated, session } = useAuth();

    if (isAuthenticated) {
        return <Navigate to={getDefaultAppRoute(session?.role, session?.studentStatus)} replace />;
    }

    return <Outlet />;
}
