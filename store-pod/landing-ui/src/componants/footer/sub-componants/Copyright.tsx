import {Store} from "@/types/store";
import {Link} from "@/navigation";

export const Copyright = ({store, t}: { store: Store, t: (it: string) => string }) => {
    return (
        <>
            <div className="copyright mb-30 ">
                <div className="footer-logo">
                    <Link href={"/"}>
                        <img alt="" src={store.logo.path}/>
                    </Link>
                </div>
                <p>
                    Copyright © 2024-2024
                    <Link href={"/"} rel="noopener noreferrer">
                        {store.name}
                    </Link>
                    .<br/>
                    All Rights Reserved
                </p>
            </div>
        </>
    )
}