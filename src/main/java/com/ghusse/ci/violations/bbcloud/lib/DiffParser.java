package com.ghusse.ci.violations.bbcloud.lib;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Ghusse on 31/03/2017.
 */
public class DiffParser {
  public DiffParser(){

  }

  public List<String> getChangedFiles(InputStream diff){
    HashSet<String> changedFiles = new HashSet<>();

    Scanner scanner = new Scanner(diff);

    boolean diffMeta = false;

    while(scanner.hasNextLine()){
      String line = scanner.nextLine();

      if (line.startsWith("diff ")){
        diffMeta = true;
      }else if (line.startsWith("@@")){
        diffMeta = false;
      } else if (line.startsWith("+++") && diffMeta){
        String changedFile = line.replace("+++ ", "");

        if (!"/dev/null".equals(changedFile)){
          changedFiles.add(changedFile.replaceFirst("^[a-z]/", ""));
        }
      }
    }

    return new ArrayList<>(changedFiles);
  }
}
