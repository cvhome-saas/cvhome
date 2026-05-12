import {extractSsrContext} from "@/services/store-context-ssr-utils";
import LoginRedirect from "./login-client";

export default async function LoginPage() {
    const storeContext = await extractSsrContext();

    return <LoginRedirect storeContext={storeContext}/>;
}
