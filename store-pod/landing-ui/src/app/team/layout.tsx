import {Metadata} from "next";

export const metadata: Metadata = {
    title:"nothing"
}
export default function Layout(p:any) {
    return (
        <main className="flex min-h-screen flex-col items-center justify-between p-24">
            welcome
            {p.children}
        </main>
    );
}