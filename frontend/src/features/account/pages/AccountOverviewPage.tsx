import { Link } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";
import EmptyState from "../../../components/ui/EmptyState";
import InfoCard from "../../../components/ui/InfoCard";
import SectionCard from "../../../components/ui/SectionCard";
import StatusBadge from "../../../components/ui/StatusBadge";
import { darkActionButtonClass, darkActionButtonStyle } from "../../../components/ui/buttonStyles";

export default function AccountOverviewPage() {
    const { session } = useAuth();

    if (!session) {
        return null;
    }

    return (
        <div className="space-y-6">
            <SectionCard
                title="Profile summary"
                description="Safe account information currently available from your authenticated session."
            >
                <div className="grid gap-4 sm:grid-cols-2">
                    <InfoCard label="Email" value={session.email} />
                    <InfoCard label="Role" value={session.role} />
                    <InfoCard label="User ID" value={String(session.userId)} />
                    <InfoCard
                        label="Student status"
                        value={session.role === "STUDENT" && session.studentStatus ? session.studentStatus.replace(/_/g, " ").toLowerCase().replace(/^\w/, (letter) => letter.toUpperCase()) : "N/A"}
                    />
                </div>
            </SectionCard>

            <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
                <SectionCard
                    title="Available account details"
                    description="This frontend currently uses authenticated session data for self-service account pages."
                >
                    <div className="space-y-4 rounded-2xl border border-slate-200 bg-slate-50 p-5">
                        <div className="flex flex-wrap items-center gap-2">
                            <StatusBadge kind="role" value={session.role} />
                            {session.role === "STUDENT" && session.studentStatus ? (
                                <StatusBadge kind="onboarding" value={session.studentStatus} />
                            ) : null}
                        </div>
                        <p className="text-sm leading-6 text-slate-600">
                            Full name and richer personal metadata are not currently available in the authenticated session payload. This account area deliberately shows only supported data instead of inventing profile information.
                        </p>
                    </div>
                </SectionCard>

                <SectionCard title="Security" description="Password and personal access settings.">
                    <p className="text-sm leading-6 text-slate-500">
                        Use the dedicated security page to update your password and review password guidance for this account.
                    </p>
                    <Link
                        to="/account/security"
                        className={`mt-5 w-full px-4 py-3 text-sm font-bold ${darkActionButtonClass}`}
                        style={darkActionButtonStyle}
                    >
                        <span className="text-white" style={darkActionButtonStyle}>Open security settings</span>
                        <span className="material-symbols-outlined text-[18px] text-white" style={darkActionButtonStyle}>shield_lock</span>
                    </Link>
                </SectionCard>
            </div>

            <EmptyState
                title="More profile controls can be added later"
                description="This account area now separates self-service from admin user management. Future profile enhancements should use a supported current-user endpoint rather than admin user APIs."
                icon="account_circle"
            />
        </div>
    );
}
