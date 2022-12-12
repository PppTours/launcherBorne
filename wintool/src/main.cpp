#include <iostream>
#include <string>

#include "entry_points.h"

int main(int argc, const char **argv)
{
  argc--; argv++; // skip program name

  if (argc == 0) {
    std::cerr << "Missing action: substitution|key_logger|send_stroke" << std::endl;
    return 1;
  }

  std::string action = argv[0];
  std::string arg1 = argc > 1 ? argv[1] : "";
  if (action == "substitution") entry_points::substitutions();
  else if (action == "key_logger") entry_points::key_logger();
  else if (action == "send_stroke") entry_points::send_stroke(arg1);
  else std::cerr << "Unknown action '" << action << "'" << std::endl;

  return 0;
}