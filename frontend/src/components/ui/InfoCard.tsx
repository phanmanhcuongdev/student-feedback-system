type InfoCardProps = {
    label: string;
    value: string;
};

export default function InfoCard({ label, value }: InfoCardProps) {
    return (
        <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-500">{label}</p>
            <p className="mt-3 text-base font-bold text-slate-950">{value}</p>
        </div>
    );
}
