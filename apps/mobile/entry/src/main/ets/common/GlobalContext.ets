/**
 * 全局上下文
 */
export class GlobalContext {
  private static instance: GlobalContext = new GlobalContext();
  private objects: Map<string, Object> = new Map();

  abilityContext: any = null;
  windowStage: any = null;

  static getContext(): GlobalContext {
    return GlobalContext.instance;
  }

  getObject(key: string): Object | undefined {
    return this.objects.get(key);
  }

  setObject(key: string, value: Object): void {
    this.objects.set(key, value);
  }
}