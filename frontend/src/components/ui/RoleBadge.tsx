import StatusBadge from "./StatusBadge";

type RoleBadgeProps = {
    value: string | null | undefined;
};

export default function RoleBadge({ value }: RoleBadgeProps) {
    return <StatusBadge kind="role" value={value} />;
}
