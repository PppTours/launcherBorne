#include "entry_points.h"

#include <iostream>
#include <semaphore>
#include <thread>
#include <functional>
#include <mutex>
#include <queue>

#include <Windows.h>

namespace key_logger {

static struct {
  std::counting_semaphore<99> semaphore{ 0 };
  std::mutex mutex;
  std::queue<std::function<void()>> events;
} s_threading;

void mainEventLoop()
{
  int errorFlag;
  MSG msg;
  while ((errorFlag = GetMessage(&msg, NULL, 0, 0)) != 0) {
    if (errorFlag == -1) {
      break;
    } else {
      TranslateMessage(&msg);
      DispatchMessage(&msg);
    }
  }
  std::cout << "main loop exited" << std::endl;
}

static LRESULT CALLBACK globalKeyboardHookProc(int code, WPARAM wParam, LPARAM lParam)
{
  if (code < 0)
    return CallNextHookEx(0, code, wParam, lParam);
  KBDLLHOOKSTRUCT keyEvent = *(KBDLLHOOKSTRUCT *)lParam;
  s_threading.mutex.lock();
  s_threading.events.push([keyEvent, wParam] {
    std::cout << "ev wParam=" << wParam << " vkCode = " << keyEvent.vkCode << " flags = " << keyEvent.flags << std::endl;
  });
  s_threading.mutex.unlock();
  s_threading.semaphore.release();

  return CallNextHookEx(0, code, wParam, lParam);
}

void start()
{
  std::thread handlerThread{ [] {
    while (true) {
      s_threading.semaphore.acquire();
      s_threading.events.front()();
      s_threading.mutex.lock();
      s_threading.events.pop();
      s_threading.mutex.unlock();
    }
  } };
  SetWindowsHookExA(WH_KEYBOARD_LL, globalKeyboardHookProc, NULL, NULL);
  mainEventLoop();
}

}