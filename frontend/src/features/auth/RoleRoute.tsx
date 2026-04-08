import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./useAuth";
import { getDefaultAppRoute } from "./defaultRoute";

type RoleRouteProps = {
    allowedRoles: string[];
};

export default function RoleRoute({ allowedRoles }: RoleRouteProps) {
    const { session } = useAuth();

    if (!session) {
        return <Navigate to="/login" replace />;
    }

    if (!allowedRoles.includes(session.role)) {
        return <Navigate to={getDefaultAppRoute(session.role, session.studentStatus)} replace />;
    }

    return <Outlet />;
}
