/**
 * HTTP 客户端封装
 */
import http from "@ohos.net.http";
import { Constants } from "../common/Constants";

export class HttpClient {
  private static instance: HttpClient = new HttpClient();
  static getInstance(): HttpClient {
    return HttpClient.instance;
  }

  private token: string = "";

  setToken(token: string) {
    this.token = token;
  }

  getToken(): string {
    return this.token;
  }

  async request(method: string, path: string, data?: any): Promise<any> {
    return new Promise((resolve, reject) => {
      const httpRequest = http.createHttp();
      const headers: any = {
        "Content-Type": "application/json",
        "Accept": "application/json"
      };
      if (this.token) {
        headers["Authorization"] = `Bearer ${this.token}`;
      }

      httpRequest.request(
        Constants.API_BASE + path,
        {
          method: method as http.RequestMethod,
          header: headers,
          extraData: data ? JSON.stringify(data) : undefined,
          connectTimeout: 15000,
          readTimeout: 30000
        }
      ).then((response: http.HttpResponse) => {
        httpRequest.destroy();
        try {
          const body = JSON.parse(response.result as string);
          if (body.code === 0) {
            resolve(body.data);
          } else if (body.code === 102001 || body.code === 102002) {
            reject({ code: body.code, message: "登录已过期" });
          } else {
            reject({ code: body.code, message: body.message || "操作失败" });
          }
        } catch (e) {
          reject({ code: -1, message: "响应解析失败" });
        }
      }).catch((err: Error) => {
        httpRequest.destroy();
        reject({ code: -1, message: err.message || "网络错误" });
      });
    });
  }

  get(path: string, params?: any): Promise<any> {
    if (params) {
      const query = Object.keys(params)
        .filter(k => params[k] !== null && params[k] !== undefined)
        .map(k => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`)
        .join("&");
      path = query ? `${path}?${query}` : path;
    }
    return this.request("GET", path);
  }

  post(path: string, data?: any): Promise<any> {
    return this.request("POST", path, data);
  }

  put(path: string, data?: any): Promise<any> {
    return this.request("PUT", path, data);
  }

  patch(path: string, data?: any): Promise<any> {
    return this.request("PATCH", path, data);
  }

  delete(path: string): Promise<any> {
    return this.request("DELETE", path);
  }

  upload(path: string, fileData: any): Promise<any> {
    return new Promise((resolve, reject) => {
      const httpRequest = http.createHttp();
      const headers: any = {
        "Content-Type": "multipart/form-data",
        "Accept": "application/json"
      };
      if (this.token) {
        headers["Authorization"] = `Bearer ${this.token}`;
      }

      const formData = new FormData();
      formData.append("file", fileData);

      httpRequest.request(
        Constants.API_BASE + path,
        {
          method: "POST",
          header: headers,
          extraData: formData,
          connectTimeout: 15000,
          readTimeout: 30000
        }
      ).then((response: http.HttpResponse) => {
        httpRequest.destroy();
        try {
          const body = JSON.parse(response.result as string);
          if (body.code === 0) {
            resolve(body.data);
          } else {
            reject({ code: body.code, message: body.message || "上传失败" });
          }
        } catch (e) {
          reject({ code: -1, message: "响应解析失败" });
        }
      }).catch((err: Error) => {
        httpRequest.destroy();
        reject({ code: -1, message: err.message || "网络错误" });
      });
    });
  }
}