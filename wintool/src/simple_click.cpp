#include "entry_points.h"

#include <Windows.h>

namespace simple_click {

void start()
{
  // send a click right in the center of the (active) screen
  INPUT inputs[2]{};
  inputs[0].type       = inputs[1].type       = INPUT_MOUSE;
  inputs[0].mi.dx      = inputs[1].mi.dx      = 65535/2;
  inputs[0].mi.dy      = inputs[1].mi.dy      = 65535/2;
  inputs[0].mi.dwFlags = inputs[1].mi.dwFlags = MOUSEEVENTF_ABSOLUTE | MOUSEEVENTF_MOVE;
  inputs[0].mi.dwFlags |= MOUSEEVENTF_LEFTDOWN;
  inputs[1].mi.dwFlags |= MOUSEEVENTF_LEFTUP;

  SendInput(2, inputs, sizeof(INPUT));
}

}