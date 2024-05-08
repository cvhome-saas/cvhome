'use client'
import {Link} from "@/navigation";

export const BreadCrumb = ({name, t}: { name: string, t: { [key: string]: string } }) => {
    return <div className="breadcrumb-area pt-35 pb-35 bg-gray-3">
        <div className="container">
            <div className="breadcrumb-content text-center">
                <span>
                        <span>
                            <Link prefetch={false} href={"/"} aria-current="page" className="active">
                                {t['Home']}
                            </Link>
                            <span>/</span>
                        </span>
                        <span>
                            {name}
                        </span>
                    </span>
            </div>
        </div>
    </div>
}
