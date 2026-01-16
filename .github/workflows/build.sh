cp -rf ./tests/checkstyle.xml ./checkstyle.xml
cp -rf ./tests/suppressions.xml ./suppressions.xml

mvn enforcer:enforce -Denforcer.rules=requireProfileIdsExist -P check --no-transfer-progress || exit 1

mvn verify -P check --no-transfer-progress
status=$?

echo "=== surefire-reports list ==="
ls -la target/surefire-reports || true

echo "=== ShareItTests report (txt) ==="

cat target/surefire-reports/*ShareItTests*.txt 2>/dev/null || true

echo "=== All surefire txt reports (fallback) ==="
for f in target/surefire-reports/*.txt; do
  echo "----- $f -----"
  cat "$f"
done 2>/dev/null || true

exit $status
