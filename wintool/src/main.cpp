#include <iostream>
#include <string>

#include <Windows.h>

namespace send_stroke {

static void single_click()
{
  // send a click right in the center of the (active) screen
  INPUT inputs[2]{};
  inputs[0].type = inputs[1].type = INPUT_MOUSE;
  inputs[0].mi.dy = inputs[1].mi.dy = 65535 / 2;
  inputs[0].mi.dx = inputs[1].mi.dx = 65535 / 2;
  inputs[0].mi.dwFlags = inputs[1].mi.dwFlags = MOUSEEVENTF_ABSOLUTE | MOUSEEVENTF_MOVE;
  inputs[0].mi.dwFlags |= MOUSEEVENTF_LEFTDOWN;
  inputs[1].mi.dwFlags |= MOUSEEVENTF_LEFTUP;
  SendInput(2, inputs, sizeof(INPUT));
}

static void hide_cursor()
{
  // send a mouse move to the right of the (active) screen
  INPUT input;
  input.type = INPUT_MOUSE;
  input.mi.dy = 65535/2;
  input.mi.dx = 65535;
  input.mi.dwFlags = MOUSEEVENTF_ABSOLUTE | MOUSEEVENTF_MOVE;
  SendInput(1, &input, sizeof(INPUT));
}

static void press_f11()
{
  INPUT inputs[2]{};
  inputs[0].type = inputs[1].type = INPUT_KEYBOARD;
  inputs[0].ki.wVk = inputs[1].ki.wVk = VK_F11;
  inputs[0].ki.wScan = inputs[1].ki.wScan = 0;
  inputs[1].ki.dwFlags |= KEYEVENTF_KEYUP;
  SendInput(2, inputs, sizeof(INPUT));
}

}

int main(int argc, const char **argv)
{
  argc--; argv++; // skip program name

  if (argc == 0) {
    std::cerr << "Missing action: focus_game|focus_launcher" << std::endl;
    return 1;
  }

  std::string action = argv[0];
  if (action == "focus_launcher") {
    send_stroke::single_click();
    send_stroke::press_f11();
    send_stroke::hide_cursor();
  } else if (action == "focus_game") {
    send_stroke::single_click();
    send_stroke::hide_cursor();
  } else {
    std::cerr << "Unknown action '" << action << "'" << std::endl;
  }

  return 0;
}