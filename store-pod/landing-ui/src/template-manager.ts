import * as path from "node:path";
import * as fs from "node:fs";
import next from "next";

export default class TemplateManager {
  private readonly nextApps = new Map<string, any>();
  private readonly initPromises = new Map<string, Promise<any>>();

  constructor(
    private readonly baseDir: string,
    private readonly env: string,
    private readonly port: number
  ) {}

  async getApp(templateName: string): Promise<any> {
    // Return if already initialized
    if (this.nextApps.has(templateName)) {
      return this.nextApps.get(templateName);
    }

    // If initialization is in progress, await it
    const existingPromise = this.initPromises.get(templateName);
    if (existingPromise) {
      return existingPromise;
    }

    // Start initialization and cache the promise
    const initPromise = this.initApp(templateName);
    this.initPromises.set(templateName, initPromise);
    return initPromise;
  }

  private initApp(templateName: string): Promise<any> {
    return (async () => {
      const originalCwd = process.cwd();

      try {
        const templateDir = path.resolve(this.baseDir, `./templates/${templateName}`);

        if (!fs.existsSync(templateDir)) {
          throw new Error(`Template '${templateName}' not found`);
        }

        // Change the working directory to template directory for next-intl and others to resolve correctly
        process.chdir(templateDir);

        // @ts-ignore — Next has its own type
        const nextApp = next({
          dev: this.env !== "production",
          dir: templateDir,
          hostname: "localhost",
          port: this.port,
        });

        await nextApp.prepare();
        this.nextApps.set(templateName, nextApp);

        return nextApp;
      } finally {
        // Restore the original working directory
        process.chdir(originalCwd);
        // Clean up the promise from the cache regardless of success/failure
        this.initPromises.delete(templateName);
      }
    })();
  }
}
