import { Link } from "react-router-dom";

export default function MainHeader() {

    return (
        <header className="bg-white dark:bg-blue-950/80 backdrop-blur-md shadow-sm dark:shadow-none docked full-width top-0 sticky z-50">
            <nav className="flex justify-between items-center px-6 py-4 w-full max-w-screen-2xl mx-auto">
                <div className="flex items-center gap-8">
                    <span className="text-xl font-bold tracking-tight text-blue-900 dark:text-blue-50 font-manrope">Insight Observatory</span>
                    <div className="hidden md:flex gap-6 font-manrope text-sm font-medium">
                        <a className="text-slate-500 dark:text-slate-400 hover:bg-blue-50 dark:hover:bg-blue-900/50 transition-colors px-2 py-1 rounded"
                           href="#">Dashboard</a>
                        <Link
                            to="/surveys"
                            className="text-blue-700 dark:text-blue-300 border-b-2 border-blue-600 px-2 py-1"
                        >
                            Surveys
                        </Link>
                        <a className="text-slate-500 dark:text-slate-400 hover:bg-blue-50 dark:hover:bg-blue-900/50 transition-colors px-2 py-1 rounded"
                           href="#">History</a>
                    </div>
                </div>
                <div className="flex items-center gap-4">
                    <div className="relative hidden sm:block">
                        <span
                            className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-lg">search</span>
                        <input
                            className="pl-10 pr-4 py-2 bg-blue-50/50 dark:bg-blue-900/20 border-none rounded-full text-sm focus:ring-2 focus:ring-primary w-64 transition-all"
                            placeholder="Search surveys..." type="text"/>
                    </div>
                    <div className="flex items-center gap-2">
                        <button
                            className="p-2 text-slate-500 hover:bg-blue-50 dark:hover:bg-blue-900/50 rounded-full transition-colors active:scale-95 duration-200" type="button">
                            <span className="material-symbols-outlined">notifications</span>
                        </button>
                        <button
                            className="p-2 text-slate-500 hover:bg-blue-50 dark:hover:bg-blue-900/50 rounded-full transition-colors active:scale-95 duration-200" type="button">
                            <span className="material-symbols-outlined">help</span>
                        </button>
                        <div className="w-8 h-8 rounded-full overflow-hidden ml-2 border border-blue-100">
                            <img alt="User profile avatar" className="w-full h-full object-cover"
                                 data-alt="Portrait of a male student smiling"
                                 src="https://lh3.googleusercontent.com/aida-public/AB6AXuClyDgxYr73ec6i08yvDcwnCcnGGdncojdaX75oDrzVRBLhMDJakTr2f4i9cbJDI28A72WElXkwrpr32Jhp3h5Q13-_SNdZVnlpfotrDn5aVW26HsmmD-mU484HtMypbwDWwC01-n1nz1ChluKk1bcboWZ7yjAW6thWchskfEMj9A8b_GJRdO7VHDZZ5D-cgi_HnapaHW-PWhd1Fcqy57HxRQG_aIZmNGGUbFHEw2xnDP7sgXD8Yp4BZgaCiS7SURBjdWeCpk2z_NY"/>
                        </div>
                    </div>
                </div>
            </nav>
        </header>
    );
}