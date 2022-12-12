#include "entry_points.h"

#include <iostream>
#include <vector>
#include <fstream>
#include <semaphore>
#include <thread>
#include <functional>
#include <mutex>
#include <queue>

#include <Windows.h>

namespace substitutions {

static struct {
  std::vector<int> replaced;
  std::vector<int> replacements;
} s_substitutions;

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

static void sendKeyboardEventLater(int replacementVkCode, KBDLLHOOKSTRUCT *keyEvent)
{
  INPUT input{};
  input.type = INPUT_KEYBOARD;
  input.ki.dwExtraInfo = keyEvent->dwExtraInfo;
  input.ki.time = keyEvent->time;
  input.ki.wScan = (WORD)keyEvent->scanCode;
  input.ki.wVk = replacementVkCode;
  input.ki.dwFlags = 0;
  if(keyEvent->flags & LLKHF_UP) input.ki.dwFlags |= KEYEVENTF_KEYUP;
  if(keyEvent->flags & LLKHF_EXTENDED) input.ki.dwFlags |= KEYEVENTF_EXTENDEDKEY;

  s_threading.mutex.lock();
  s_threading.events.push([input] {
    SendInput(1, (INPUT*)&input, sizeof(INPUT));
  });
  s_threading.mutex.unlock();
  s_threading.semaphore.release();
}

static LRESULT CALLBACK globalKeyboardHookProc(int code, WPARAM wParam, LPARAM lParam)
{
  if (code < 0)
    return CallNextHookEx(0, code, wParam, lParam);
  KBDLLHOOKSTRUCT *keyEvent = (KBDLLHOOKSTRUCT *)lParam;
  //std::cout << keyEvent->vkCode << std::endl;
  for (size_t i = 0; i < s_substitutions.replaced.size(); i++) {
    if (s_substitutions.replaced[i] == keyEvent->vkCode) {
      sendKeyboardEventLater(s_substitutions.replacements[i], keyEvent);
      return true;
    }
  }
  return CallNextHookEx(0, code, wParam, lParam);
}

static void loadKeySubstitutions()
{
  std::ifstream file{ "keys.txt" };
  if (!file.good())
    throw std::exception("Missing keys.txt file!");
  int replaced, replacement;
  std::cout << "replacements: ";
  while (file.good()) {
    file >> replaced >> replacement;
    s_substitutions.replaced.push_back(replaced);
    s_substitutions.replacements.push_back(replacement);
    std::cout << replaced << "|" << replacement << " ";
  }
  std::cout << std::endl;
}

void start()
{
  try {
    loadKeySubstitutions();
  } catch (const std::exception &e) {
    std::cerr << e.what() << std::endl;
    return;
  }
  std::thread handlerThread{ [] {
    while (true) {
      s_threading.semaphore.acquire();
      s_threading.events.front()();
      {
        std::lock_guard _lock{ s_threading.mutex };
        s_threading.events.pop();
      }
    }
  } };
  handlerThread.detach();
  SetWindowsHookExA(WH_KEYBOARD_LL, globalKeyboardHookProc, NULL, NULL);
  mainEventLoop();
}

}