import { Link } from "react-router-dom";

export default function MainHeader() {
    return (
        <header className="sticky top-0 z-50 bg-white shadow-sm backdrop-blur-md">
            <nav className="flex justify-between items-center px-6 py-4 w-full max-w-screen-2xl mx-auto">
                <div className="flex items-center gap-8">
                    <span className="text-xl font-bold tracking-tight text-blue-900 font-manrope">
                        Insight Observatory
                    </span>
                    <div className="hidden md:flex gap-6 font-manrope text-sm font-medium">
                        <Link
                            to="/surveys"
                            className="border-b-2 border-blue-600 px-2 py-1 text-blue-700"
                        >
                            Surveys
                        </Link>
                    </div>
                </div>

                <div className="flex items-center gap-4">
                    <span className="rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold uppercase tracking-wider text-blue-700">
                        MVP Survey Portal
                    </span>
                </div>
            </nav>
        </header>
    );
}
