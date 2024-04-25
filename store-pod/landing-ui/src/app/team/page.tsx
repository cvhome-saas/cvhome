import {Suspense} from "react";
import {cookies, headers} from "next/headers";
import {Test} from "@/app/team/test";

async function wait(duration: number) {
    return new Promise(resolve => {
        setTimeout(resolve, duration)
    })
}

export default async function Team(p:any) {
    console.log(p.searchParams)
    console.log(cookies().getAll())
    console.log(headers().get('store'))
    return (
        <main className="flex min-h-screen flex-col items-center justify-between p-24">
            hello
            <Test/>
            <Suspense>
                <LoadTeam/>
            </Suspense>
        </main>
    );
}

async function LoadTeam() {
    const result = 4000;
    return (<>result {result}</>)
}