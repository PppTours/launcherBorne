#include "entry_points.h"

#include <iostream>

#include <Windows.h>

namespace send_stroke {

static void single_click()
{
  // send a click right in the center of the (active) screen
  INPUT inputs[3]{};
  inputs[0].type  = inputs[1].type  = inputs[2].type = INPUT_MOUSE;
  inputs[0].mi.dy = inputs[1].mi.dy = inputs[2].mi.dy = 65535 / 2;
  inputs[0].mi.dx = inputs[1].mi.dx = 65535 / 2;
  inputs[2].mi.dx = 65535;
  inputs[0].mi.dwFlags = inputs[1].mi.dwFlags = inputs[2].mi.dwFlags = MOUSEEVENTF_ABSOLUTE | MOUSEEVENTF_MOVE;
  inputs[0].mi.dwFlags |= MOUSEEVENTF_LEFTDOWN;
  inputs[1].mi.dwFlags |= MOUSEEVENTF_LEFTUP;
  SendInput(3, inputs, sizeof(INPUT));
}

static void press_f11()
{
  INPUT inputs[2]{};
  inputs[0].type     = inputs[1].type     = INPUT_KEYBOARD;
  inputs[0].ki.wVk   = inputs[1].ki.wVk   = VK_F11;
  inputs[0].ki.wScan = inputs[1].ki.wScan = 0;
  inputs[1].ki.dwFlags |= KEYEVENTF_KEYUP;
  SendInput(2, inputs, sizeof(INPUT));
}

void start(const std::string &arg)
{
  if (arg == "single_click") single_click();
  else if (arg == "f11") press_f11();
  else {
    std::cerr << "Invalid stroke action '" << arg << "'" << std::endl;
  }
}

}