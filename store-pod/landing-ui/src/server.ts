import * as path from "node:path";
import * as fs from "node:fs";
import type {NextFunction, Request, Response} from "express";
import express from "express";
import next from "next";

const __dirname = process.cwd();


const env: string = process.env.NODE_ENV || "development";
const port = 8110;

// Express app setup
const app = express();

// Cache Next.js apps per template
const nextApps = new Map<string, any>();
// Cache initialization promises to prevent concurrent initialization
const initPromises = new Map<string, Promise<any>>();

async function getNextApp(templateName: string) {
    // If the app is already initialized, return it
    if (nextApps.has(templateName)) {
        return nextApps.get(templateName);
    }

    // If initialization is in progress, wait for it
    if (initPromises.has(templateName)) {
        return initPromises.get(templateName);
    }

    // Start initialization and cache the promise
    const initPromise = getInitPromise(templateName);
    initPromises.set(templateName, initPromise);
    return initPromise;
}

function getInitPromise(templateName: string): Promise<any> {
    return (async () => {
        const originalCwd = process.cwd();

        try {
            const templateDir = path.resolve(__dirname, `./templates/${templateName}`);

            if (!fs.existsSync(templateDir)) {
                throw new Error(`Template '${templateName}' not found`);
            }

            // Change working directory to template directory
            // This is necessary for next-intl and other plugins to resolve paths correctly
            process.chdir(templateDir);

            // @ts-ignore
            const nextApp = next({
                dev: env !== "production",
                dir: templateDir,
                hostname: "localhost",
                port,
            });

            await nextApp.prepare();
            nextApps.set(templateName, nextApp);

            return nextApp;
        } finally {
            // Restore the original working directory
            process.chdir(originalCwd);

            // Clean up the promise from cache after completion
            initPromises.delete(templateName);
        }
    })();
}

function getTheme(req: Request) {
    const rawTheme = req.headers['Theme'];
    const themeName = Array.isArray(rawTheme) ? rawTheme[0] : rawTheme ?? "default";
    return themeName.toLowerCase();
}

app.get("*", async (req: Request, res: Response, next: NextFunction) => {
    try {

        const theme = getTheme(req);
        console.log(`ThemeName: ${theme} | Request URL: ${req.originalUrl}`);

        const nextApp = await getNextApp(theme);
        const handle = nextApp.getRequestHandler();

        await handle(req, res);
    } catch (err) {
        next(err);
    }
});

app.listen(port, () => {
    console.log(`✅ Store Front running on http://localhost:${port} env: ${env}`);
});