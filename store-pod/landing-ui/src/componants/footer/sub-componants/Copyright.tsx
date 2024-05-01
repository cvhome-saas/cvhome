import {Store} from "@/types/store";

export const Copyright = ({store, t}: { store: Store, t: (it: string) => string }) => {
    return (
        <>
            <div className="copyright mb-30 ">
                <div className="footer-logo">
                    <a href="/">
                        <img alt="" src={store.logo.path}/>
                    </a>
                </div>
                <p>Copyright © 2024-2024 <a rel="noopener noreferrer" href="/">
                    {store.name}</a>.<br/>
                    All Rights Reserved
                </p>
            </div>
        </>
    )
}