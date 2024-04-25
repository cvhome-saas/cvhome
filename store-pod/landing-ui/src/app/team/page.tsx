import {Suspense} from "react";

async function wait(duration: number) {
    return new Promise(resolve => {
        setTimeout(resolve, duration)
    })
}

export default async function Team() {
    return (
        <main className="flex min-h-screen flex-col items-center justify-between p-24">
            hello
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